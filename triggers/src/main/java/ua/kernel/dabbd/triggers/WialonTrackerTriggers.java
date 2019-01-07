package ua.kernel.dabbd.triggers;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.serde.JacksonDeserializationSchema;
import ua.kernel.dabbd.commons.serde.JacksonSerializationSchema;
import ua.kernel.dabbd.triggers.functions.ProcessDataGap;
import ua.kernel.dabbd.triggers.functions.ProcessFuelLevel;
import ua.kernel.dabbd.triggers.functions.ProcessPowerLost;
import ua.kernel.dabbd.triggers.functions.ProcessSignalLost;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public class WialonTrackerTriggers {

    private static final String DEFAULT_TRACKERS_TOPIC = "WIALON_test";

    private static final int LOST_TRACKER_TIMEOUT_SECONDS = 5 * 60;
    private static final ZoneOffset OFFSET = OffsetDateTime.now().getOffset();
    private static final String DEFAULT_BROKERS = "ks-dmp-dev14.kyivstar.ua:6667,ks-dmp-dev15.kyivstar.ua:6667";
    private static final String DEFAULT_TRIGGERS_TOPIC = "triggers";

    private static final long DEFAULT_DATA_GAP_TIMEGAP_SECONDS = 3 * 60;
    private static final int DEFAULT_DATA_GAP_DISTANCE_METERS = 500;
    private static final int DEFAULT_DATA_GAP_SPEED_KMH = 10;
    private static final int LOST_SIGNAL_EVALUATION_PERIOD_SECONDS = 60;
    private static final int DEFAULT_FUEL_LEVEL_SPIKE = 10;


    public static void main(String[] args) throws Exception {
        log.info("> WialonTopicTriggersExample >>>>>>>");

        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        log.info("Params for WialonTrackerTriggers: {}", parameterTool.getProperties());

        String trackersTopic = parameterTool.get("trackers.topic", DEFAULT_TRACKERS_TOPIC);
        String triggersTopic = parameterTool.get("triggers.topic", DEFAULT_TRIGGERS_TOPIC);
        String groupId = parameterTool.get("group.id", "lost-tracker-group-" + UUID.randomUUID().toString());
        String bootstrapServers = parameterTool.get("bootstrap.servers", DEFAULT_BROKERS);
        String autoOffsetReset = parameterTool.get("auto.offset.reset", "latest");

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("group.id", groupId);
        properties.setProperty("auto.offset.reset", autoOffsetReset);
        log.info("kafka props: {}", properties);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        FlinkKafkaConsumer<TrackerEvent> trackerEventsKafkaSource = new FlinkKafkaConsumer<>(trackersTopic, new JacksonDeserializationSchema<>(TrackerEvent.class), properties);
        if (autoOffsetReset.equals("latest")) trackerEventsKafkaSource.setStartFromLatest();

        int lostTrackerTimeoutSeconds = parameterTool.getInt("lost.tracker.timeout.seconds", LOST_TRACKER_TIMEOUT_SECONDS);
        BoundedOutOfOrdernessTimestampExtractor<TrackerEvent> timestampAndWatermarkAssigner = getTimestampAndWatermarkAssigner(lostTrackerTimeoutSeconds);

        DataStream<TrackerEvent> stream = env
                .addSource(trackerEventsKafkaSource)
                .assignTimestampsAndWatermarks(timestampAndWatermarkAssigner);

        KeyedStream<TrackerEvent, String> streamByTrackerId = stream.keyBy(TrackerEvent::getTrackerId);

        SingleOutputStreamOperator<EventTrigger> process = streamByTrackerId
                .countWindow(1)//ignored due to specified time trigger below
                .trigger(ProcessSignalLost.processTimeLostTrackerTrigger(LOST_SIGNAL_EVALUATION_PERIOD_SECONDS))
                .process(new ProcessSignalLost(lostTrackerTimeoutSeconds, timestampAndWatermarkAssigner));
        process.addSink(getEventTriggerSink(triggersTopic, properties));
        process.print("SIGNAL_LOST >>>");


        long dataGapTimegapSeconds = parameterTool.getLong("data.gap.timegap.seconds", DEFAULT_DATA_GAP_TIMEGAP_SECONDS);
        int dataGapDistanceMeters = parameterTool.getInt("data.gap.distance.meters", DEFAULT_DATA_GAP_DISTANCE_METERS);
        int dataGapSpeedKmh = parameterTool.getInt("data.gap.speed.kmh", DEFAULT_DATA_GAP_SPEED_KMH);

        SingleOutputStreamOperator<EventTrigger> processedDataGap = streamByTrackerId
                .countWindow(2)
                .process(new ProcessDataGap(dataGapTimegapSeconds, dataGapDistanceMeters, dataGapSpeedKmh));
        processedDataGap.addSink(getEventTriggerSink(triggersTopic, properties));
        processedDataGap.print("TRACKER_DATA_GAP >>>");


        SingleOutputStreamOperator<EventTrigger> processedFuelLevel = streamByTrackerId
                .countWindow(6, 1)
                .process(new ProcessFuelLevel(DEFAULT_FUEL_LEVEL_SPIKE));
        processedFuelLevel.addSink(getEventTriggerSink(triggersTopic, properties));
        processedFuelLevel.print("FUEL_LEVEL_JUMP >>>");


        SingleOutputStreamOperator<EventTrigger> processPowerLost = streamByTrackerId.process(new ProcessPowerLost());
        processPowerLost.addSink(getEventTriggerSink(triggersTopic, properties));
        processPowerLost.print("POWER_LOST >>>");

        env.execute();

    }

    private static BoundedOutOfOrdernessTimestampExtractor<TrackerEvent> getTimestampAndWatermarkAssigner(int lostTrackerTimeoutSeconds) {
        return new BoundedOutOfOrdernessTimestampExtractor<TrackerEvent>(Time.seconds(lostTrackerTimeoutSeconds)) {
            @Override
            public long extractTimestamp(TrackerEvent element) {
                return element.getEventDt().toInstant(OFFSET).toEpochMilli();
            }
        };
    }

    private static FlinkKafkaProducer<EventTrigger> getEventTriggerSink(String triggersTopic, Properties properties) {
        return new FlinkKafkaProducer<>(triggersTopic, new JacksonSerializationSchema<>(EventTrigger.class), properties, Optional.of(new FlinkKafkaPartitioner<EventTrigger>() {

            @Override
            public int partition(EventTrigger record, byte[] key, byte[] value, String targetTopic, int[] partitions) {
                int length = partitions.length;
                int index = Math.abs(record.getTrackerId().hashCode() % length);
                return partitions[index];
            }
        }));
    }

}

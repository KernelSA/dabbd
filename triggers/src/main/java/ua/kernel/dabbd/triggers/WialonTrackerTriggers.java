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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.serde.JacksonDeserializationSchema;
import ua.kernel.dabbd.commons.serde.JacksonSerializationSchema;
import ua.kernel.dabbd.triggers.config.TriggerParam;
import ua.kernel.dabbd.triggers.functions.ProcessDataGap;
import ua.kernel.dabbd.triggers.functions.ProcessFuelLevel;
import ua.kernel.dabbd.triggers.functions.ProcessPowerLostWindow;
import ua.kernel.dabbd.triggers.functions.ProcessSignalLost;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public class WialonTrackerTriggers {

    private static final ZoneOffset OFFSET = OffsetDateTime.now().getOffset();

    /**
     * Triggers application params
     */
    private static final String TRACKERS_TOPIC_ARG = "trackers.topic";
    private static final String TRIGGERS_TOPIC_ARG = "triggers.topic";
    private static final String GROUP_ID_ARG = "group.id";
    private static final String BOOTSTRAP_SERVERS_ARG = "bootstrap.servers";
    private static final String AUTO_OFFSET_RESET_ARG = "auto.offset.reset";
    private static final String DEFAULT_AUTO_OFFSET_RESET = "latest";

    /**
     * Default kafka param values
     */
    private static final String DEFAULT_TRACKERS_TOPIC = "WIALON";
    private static final String DEFAULT_TRIGGERS_TOPIC = "TRIGGERS";
    private static final String DEFAULT_BROKERS = "cf0:9092,cf1:9092";

    public static void main(String[] args) throws Exception {
        log.info("> WialonTopicTriggersExample >>>>>>>");

        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        log.info("Params for WialonTrackerTriggers: {}", parameterTool.getProperties());

        String trackersTopic = parameterTool.get(TRACKERS_TOPIC_ARG, DEFAULT_TRACKERS_TOPIC);
        String triggersTopic = parameterTool.get(TRIGGERS_TOPIC_ARG, DEFAULT_TRIGGERS_TOPIC);
        String groupId = parameterTool.get(GROUP_ID_ARG, "lost-tracker-group-" + UUID.randomUUID().toString());
        String bootstrapServers = parameterTool.get(BOOTSTRAP_SERVERS_ARG, DEFAULT_BROKERS);
        String autoOffsetReset = parameterTool.get(AUTO_OFFSET_RESET_ARG, DEFAULT_AUTO_OFFSET_RESET);

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        log.info("kafka props: {}", properties);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        FlinkKafkaConsumer<TrackerEvent> trackerEventsKafkaSource = new FlinkKafkaConsumer<>(trackersTopic, new JacksonDeserializationSchema<>(TrackerEvent.class), properties);
        if (autoOffsetReset.equals("latest")) trackerEventsKafkaSource.setStartFromLatest();

        int lostTrackerTimeoutSeconds = parameterTool.getInt(TriggerParam.LOST_TRACKER_TIMEOUT_SECONDS.key(), TriggerParam.LOST_TRACKER_TIMEOUT_SECONDS.defaultValue());

        DataStream<TrackerEvent> stream = env
                .addSource(trackerEventsKafkaSource)
                .assignTimestampsAndWatermarks(getTimestampAndWatermarkAssigner(lostTrackerTimeoutSeconds));

        KeyedStream<TrackerEvent, String> streamByTrackerId = stream.keyBy(TrackerEvent::getTrackerId);

        int lostTrackerSpeedThreshold = parameterTool.getInt(TriggerParam.LOST_TRACKER_SPEED_THRESHOLD.key(), TriggerParam.LOST_TRACKER_SPEED_THRESHOLD.defaultValue());
        int lostTrackerPowerThreshold = parameterTool.getInt(TriggerParam.LOST_TRACKER_POWER_THRESHOLD.key(), TriggerParam.LOST_TRACKER_POWER_THRESHOLD.defaultValue());
        SingleOutputStreamOperator<EventTrigger> process = streamByTrackerId
                .countWindow(1)//ignored due to specified time trigger below
                .trigger(ProcessSignalLost.processTimeLostTrackerTrigger(TriggerParam.LOST_SIGNAL_EVALUATION_PERIOD_SECONDS))
                .process(new ProcessSignalLost(lostTrackerTimeoutSeconds, lostTrackerSpeedThreshold, lostTrackerPowerThreshold));
        process.addSink(getEventTriggerSink(triggersTopic, properties));
        process.print("SIGNAL_LOST >>>");


        int dataGapTimegapSeconds = parameterTool.getInt(TriggerParam.DATA_GAP_TIMEGAP_SECONDS.key(), TriggerParam.DATA_GAP_TIMEGAP_SECONDS.defaultValue());
        int dataGapDistanceMeters = parameterTool.getInt(TriggerParam.DATA_GAP_DISTANCE_METERS.key(), TriggerParam.DATA_GAP_DISTANCE_METERS.defaultValue());
        int dataGapSpeedKmh = parameterTool.getInt(TriggerParam.DATA_GAP_SPEED_KMH.key(), TriggerParam.DATA_GAP_SPEED_KMH.defaultValue());

        SingleOutputStreamOperator<EventTrigger> processedDataGap = streamByTrackerId
                .countWindow(2, 1)
                .process(new ProcessDataGap(dataGapTimegapSeconds, dataGapDistanceMeters, dataGapSpeedKmh));
        processedDataGap.addSink(getEventTriggerSink(triggersTopic, properties));
        processedDataGap.print("TRACKER_DATA_GAP >>>");

        int fuelLevelSpikeArg = parameterTool.getInt(TriggerParam.FUEL_LEVEL_SPIKE.key(), TriggerParam.FUEL_LEVEL_SPIKE.defaultValue());
        int fuelLevelWindowSize = parameterTool.getInt(TriggerParam.FUEL_LEVEL_WIDOW_SIZE.key(), TriggerParam.FUEL_LEVEL_WIDOW_SIZE.defaultValue());
        SingleOutputStreamOperator<EventTrigger> processedFuelLevel = streamByTrackerId
                .countWindow(fuelLevelWindowSize, 1)
                .process(new ProcessFuelLevel(fuelLevelSpikeArg));
        processedFuelLevel.addSink(getEventTriggerSink(triggersTopic, properties));
        processedFuelLevel.print("FUEL_LEVEL_JUMP >>>");


        int powerLostWindowSize = parameterTool.getInt(TriggerParam.POWER_LOST_WINDOW_SIZE.key(), TriggerParam.POWER_LOST_WINDOW_SIZE.defaultValue());
        int powerLostZeroPowerCount = parameterTool.getInt(TriggerParam.POWER_LOST_ZERO_POWER_COUNT.key(), TriggerParam.POWER_LOST_ZERO_POWER_COUNT.defaultValue());
        int powerLostSpeedLimit = parameterTool.getInt(TriggerParam.POWER_LOST_SPEED_LIMIT.key(), TriggerParam.POWER_LOST_SPEED_LIMIT.defaultValue());
        SingleOutputStreamOperator<EventTrigger> processPowerLost = streamByTrackerId
                .countWindow(powerLostWindowSize, 1)
                .process(new ProcessPowerLostWindow(powerLostZeroPowerCount, powerLostSpeedLimit));
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

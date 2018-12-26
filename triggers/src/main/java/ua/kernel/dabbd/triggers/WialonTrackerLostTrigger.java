package ua.kernel.dabbd.triggers;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.PurgingTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.serde.JacksonDeserializationSchema;
import ua.kernel.dabbd.commons.serde.JacksonSerializationSchema;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static ua.kernel.dabbd.commons.model.TriggerType.SIGNAL_LOST;

@Slf4j
public class WialonTrackerLostTrigger {

    private static final String DEFAULT_TRACKERS_TOPIC = "WIALON_test";

    private static final int LOST_TRACKER_TIMEOUT_SECONDS = 5 * 60;
    private static final ZoneOffset OFFSET = OffsetDateTime.now().getOffset();
    private static final String DEFAULT_BROKKERS = "ks-dmp-dev14.kyivstar.ua:6667,ks-dmp-dev15.kyivstar.ua:6667";
    private static final String DEFAULT_TRIGGERS_TOPIC = "triggers";

    public static void main(String[] args) throws Exception {
        log.info("> ua.kernel.dabbd.triggers.WialonTopicTriggers >>>>>>>");

        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        log.info("Params for WialonTrackerLostTrigger: {}", parameterTool.getProperties());

        String trackersTopic = parameterTool.get("trackers.topic", DEFAULT_TRACKERS_TOPIC);
        String triggersTopic = parameterTool.get("triggers.topic", DEFAULT_TRIGGERS_TOPIC);
        int lostTrackerTimeoutSeconds = parameterTool.getInt("lost.tracker.timeout.seconds", LOST_TRACKER_TIMEOUT_SECONDS);

        String groupId = parameterTool.get("group.id", "test-group-" + UUID.randomUUID().toString());
        String bootstrapServers = parameterTool.get("bootstrap.servers", DEFAULT_BROKKERS);
        String autoOffsetReset = parameterTool.get("auto.offset.reset", "earliest");

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServers);
//        properties.setProperty("group.id", "test-group-7676aa3f-5496-4991-954d-44649e75dd2e");
        properties.setProperty("group.id", groupId);
        properties.setProperty("auto.offset.reset", autoOffsetReset);
        log.info("kafka props: {}", properties);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        FlinkKafkaConsumer<TrackerEvent> trackerEventsKafkaSource = new FlinkKafkaConsumer<>(trackersTopic, new JacksonDeserializationSchema<>(TrackerEvent.class), properties);

        FlinkKafkaProducer<EventTrigger> kafkaProducer = new FlinkKafkaProducer<>(triggersTopic, new JacksonSerializationSchema<>(EventTrigger.class), properties);
        kafkaProducer.setWriteTimestampToKafka(true);

        DataStream<TrackerEvent> stream = env
                .addSource(trackerEventsKafkaSource)
                .assignTimestampsAndWatermarks(
                        new BoundedOutOfOrdernessTimestampExtractor<TrackerEvent>(Time.seconds(lostTrackerTimeoutSeconds)) {
                            @Override
                            public long extractTimestamp(TrackerEvent element) {
                                return element.getEventDt().toEpochSecond(OFFSET);
                            }
                        }
                );

        // stream.print("raw: ");

        SingleOutputStreamOperator<EventTrigger> process = stream
                .keyBy(TrackerEvent::getTrackerId)
                .countWindow(1).trigger(PurgingTrigger.of(ContinuousProcessingTimeTrigger.of(Time.seconds(LOST_TRACKER_TIMEOUT_SECONDS))))
                .process(processWindowFunction());

        process.addSink(kafkaProducer);
        process.print("\n\nSIGNAL_LOST >>>");

        env.execute();

    }

    private static ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> processWindowFunction() {
        return new ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow>() {

            @Override
            public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

                List<LocalDateTime> eventDates = new ArrayList<>();
                for (TrackerEvent event : elements) {
                    eventDates.add(event.getEventDt());
                }

                // Zero or 1 event in LOST_TRACKER_TIMEOUT_MINUTES = SIGNAL_LOST event
                if (eventDates.size() <= 1) {
                    EventTrigger eventTrigger = new EventTrigger();
                    eventTrigger.setTrackerId(key);
                    eventTrigger.setTriggerDt(LocalDateTime.now());
                    eventTrigger.setTriggerInfo("Count of elements: " + eventDates.size() + ", eventsDt: " + eventDates.toString());
                    eventTrigger.setTriggerType(SIGNAL_LOST);

                    eventTrigger.setEventDt(eventDates.size() > 0 ? eventDates.get(0) : null);
                    out.collect(eventTrigger);
                }
//                else {
//                    System.out.println("\t\t//" + LocalDateTime.now() + "// Tracker: " + key + " Count in window: " + count + " dates: " + eventDates.toString());
//                }

            }
        };
    }
}

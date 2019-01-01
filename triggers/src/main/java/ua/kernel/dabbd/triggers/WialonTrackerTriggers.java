package ua.kernel.dabbd.triggers;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
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
import ua.kernel.dabbd.commons.util.DabBdUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static ua.kernel.dabbd.commons.model.TriggerType.*;

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
    public static final int DEFAULT_FUEL_LEVEL_SPIKE = 10;


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

        KeyedStream<TrackerEvent, String> streamByTrackerId = stream
                .keyBy(TrackerEvent::getTrackerId);

        SingleOutputStreamOperator<EventTrigger> process = streamByTrackerId
                .countWindow(1)//ignored due to specified time trigger below
                .trigger(processTimeLostTrackerTrigger(lostTrackerTimeoutSeconds))
                .process(processWindowFunction());
        process.addSink(new FlinkKafkaProducer<>(triggersTopic, new JacksonSerializationSchema<>(EventTrigger.class), properties));
        process.print("\nSIGNAL_LOST >>>");


        long dataGapTimegapSeconds = parameterTool.getLong("data.gap.timegap.seconds", DEFAULT_DATA_GAP_TIMEGAP_SECONDS);
        int dataGapDistanceMeters = parameterTool.getInt("data.gap.distance.meters", DEFAULT_DATA_GAP_DISTANCE_METERS);
        int dataGapSpeedKmh = parameterTool.getInt("data.gap.speed.kmh", DEFAULT_DATA_GAP_SPEED_KMH);

        SingleOutputStreamOperator<EventTrigger> processedDataGap = streamByTrackerId
                .countWindow(2)
                .process(processDataGap(dataGapTimegapSeconds, dataGapDistanceMeters, dataGapSpeedKmh));
        processedDataGap.addSink(new FlinkKafkaProducer<>(triggersTopic, new JacksonSerializationSchema<>(EventTrigger.class), properties));
        processedDataGap.print("\nTRACKER_DATA_GAP >>>");


        SingleOutputStreamOperator<EventTrigger> processedFuelLevel = streamByTrackerId
                .countWindow(6)
                .process(processFuelLevel());
        processedFuelLevel.addSink(new FlinkKafkaProducer<>(triggersTopic, new JacksonSerializationSchema<>(EventTrigger.class), properties));
        processedFuelLevel.print("\nFUEL_LEVEL_JUMP >>>");


        SingleOutputStreamOperator<EventTrigger> processPowerLost = streamByTrackerId.process(new KeyedProcessFunction<String, TrackerEvent, EventTrigger>() {
            @Override
            public void processElement(TrackerEvent value, Context ctx, Collector<EventTrigger> out) throws Exception {
                if (value.getSpeed() > 0 && value.getPowerLevel() < 0) {
                    EventTrigger eventTrigger = new EventTrigger();
                    eventTrigger.setTrackerId(value.getTrackerId());
                    eventTrigger.setTriggerDt(LocalDateTime.now());
                    eventTrigger.setTriggerInfo("Speed: " + value.getSpeed() + " km/h, powerLevel: " + value.getPowerLevel());
                    eventTrigger.setTriggerType(POWER_LOST);

                    eventTrigger.setEventDt(value.getEventDt());
                    out.collect(eventTrigger);
                }
            }
        });
        processPowerLost.addSink(new FlinkKafkaProducer<>(triggersTopic, new JacksonSerializationSchema<>(EventTrigger.class), properties));
        processPowerLost.print("\nPOWER_LOST >>>");

        env.execute();

    }

    private static PurgingTrigger<Object, GlobalWindow> processTimeLostTrackerTrigger(int lostTrackerTimeoutSeconds) {
        return PurgingTrigger.of(ContinuousProcessingTimeTrigger.of(Time.seconds(lostTrackerTimeoutSeconds)));
    }

    private static ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> processWindowFunction() {
        return new ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow>() {

            @Override
            public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

                List<LocalDateTime> eventDates = new ArrayList<>();
                for (TrackerEvent event : elements) {
                    eventDates.add(event.getEventDt());
                }

                // Zero or 1 event in lostTrackerTimeoutSeconds = SIGNAL_LOST event
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

    private static ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> processDataGap(long timeGap, int distanceGap, int speed) {
        return new ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow>() {

            @Override
            public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

                List<TrackerEvent> events = new ArrayList<>();
                elements.forEach(events::add);

                if (events.size() != 2) {
                    log.warn("For data-gap trigger window should contain 2 elements");
                    return;
                }

                TrackerEvent trackerEvent1 = events.get(0);
                TrackerEvent trackerEvent2 = events.get(2);

                Duration duration = Duration.between(trackerEvent1.getEventDt(), trackerEvent2.getEventDt());
                if (duration.getSeconds() < timeGap)
                    return;

                if (trackerEvent1.getSpeed() < speed || trackerEvent2.getSpeed() < speed)
                    return;

                ArrayList<Double> coordinates1 = trackerEvent1.getCoordinates();
                ArrayList<Double> coordinates2 = trackerEvent2.getCoordinates();
                double distanceM = DabBdUtils.distance(coordinates1.get(0), coordinates1.get(1), coordinates2.get(0), coordinates2.get(1), 'K') / 1000;
                if (distanceM < distanceGap)
                    return;


                EventTrigger eventTrigger = new EventTrigger();
                eventTrigger.setTrackerId(key);
                eventTrigger.setTriggerDt(LocalDateTime.now());
                eventTrigger.setTriggerInfo("Time gap: " + duration.getSeconds() + " s, speed1: "
                        + trackerEvent1.getSpeed() + " km/h, speed2: "
                        + trackerEvent2.getSpeed() + " km/h, distance: "
                        + distanceM + " m.");
                eventTrigger.setTriggerType(TRACKER_DATA_GAP);
                eventTrigger.setEventDt(trackerEvent2.getEventDt());

                out.collect(eventTrigger);

            }
        };
    }

    private static ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> processFuelLevel() {
        return new ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow>() {

            @Override
            public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

                List<TrackerEvent> events = new ArrayList<>();
                elements.forEach(events::add);

                if (events.size() != 6) {
                    log.warn("For data-gap trigger window should contain 2 elements");
                    return;
                }

                TrackerEvent trackerEvent1 = events.get(0);
                Integer fuelLevel1 = trackerEvent1.getFuelLevel();
                int fuelBaseLevel = fuelLevel1 > 0 ? fuelLevel1 : 1;
                ArrayList<Integer> agg = new ArrayList<>();
                for (int i = 1; i < 6; i++) {
                    TrackerEvent trackerEvent = events.get(i);
                    Integer fuelLevelN = trackerEvent.getFuelLevel();
                    int diff = fuelLevel1 - fuelLevelN;
                    agg.add(((Math.abs(diff / fuelBaseLevel)) * 100 < DEFAULT_FUEL_LEVEL_SPIKE) ? 0 : Integer.signum(diff));
                }

                if (agg.stream().anyMatch(d -> d == 0)) {
                    // not all 5 levels had a diff > N% -> not match trigger definition
                    return;
                }
                if (Math.abs(agg.stream().mapToInt(value -> value).sum()) != 5) {
                    // there was + and - spikes ing fuel level -> not match trigger definition
                    return;
                }

                EventTrigger eventTrigger = new EventTrigger();
                eventTrigger.setTrackerId(key);
                eventTrigger.setTriggerDt(LocalDateTime.now());
                eventTrigger.setTriggerInfo("");
                eventTrigger.setTriggerType(FUEL_LEVEL_JUMP);
                eventTrigger.setEventDt(trackerEvent1.getEventDt());

                out.collect(eventTrigger);

            }
        };
    }

}

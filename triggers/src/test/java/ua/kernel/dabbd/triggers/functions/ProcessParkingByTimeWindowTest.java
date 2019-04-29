package ua.kernel.dabbd.triggers.functions;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.source.FromIteratorFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.experimental.CollectSink;
import org.junit.Before;
import org.junit.Test;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.triggers.config.TriggerParam;
import ua.kernel.dabbd.triggers.functions.util.TestCollector;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.StringJoiner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static ua.kernel.dabbd.triggers.WialonTrackerTriggers.getTimestampAndWatermarkAssigner;

public class ProcessParkingByTimeWindowTest {

    private static final LocalDateTime TEST_TIME = LocalDateTime.now();
    String[] args = {"--test"};
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    private ProcessParkingByTimeWindow sut = new ProcessParkingByTimeWindow(parameterTool);

    private ArrayList<TrackerEvent> events;


    @Before
    public void setup() {
        events = new ArrayList<>();
        StringJoiner stringJoiner = new StringJoiner("\n>\t");
        // Events that will cause the trigger
        for (int i = 0; i < 5; i++) {
            int n = i;
            TrackerEvent trackerEvent = new TrackerEvent();
            trackerEvent.setFuelLevel(115);
            trackerEvent.setEventDt(TEST_TIME.plusSeconds(30 * n));
            trackerEvent.setSpeed(1);
            trackerEvent.setCoordinates(new ArrayList<Double>() {{
                add(50.07);
                add(31.51 + (n / 100000D));
            }});
            events.add(trackerEvent);
            stringJoiner.add(trackerEvent.toString());
        }
//        System.out.println("Events >> " + stringJoiner.toString());

    }

    @Test
    public void testProcessParking_byTimeWindow() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        sut.process("", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(1));

        assertThat(out.list.get(0).getTriggerInfo(), containsString("avgSpeed: 1.0, maxDistanceM: 2.85"));
    }


    @Test
    public void testProcessParking_TriggerNotFired_speedExceedLimit() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        // Fuel deviation not enough to fire the Trigger
        events.get(0).setSpeed(10);
        sut.process("", null, events, out);

        assertThat(out.list.size(), is(0));

    }

    @Test
    public void testProcessParking_TriggerNotFired_distanceExceedLimit() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        // Fuel deviation not enough to fire the Trigger
        events.get(4).setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(32.0);
        }});
        sut.process("", null, events, out);

        assertThat(out.list.size(), is(0));

    }

//    @Test
//    public void testFlinkStream() throws Exception {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//        // configure your test environment
//        env.setParallelism(1);
//
//        // values are collected in a static variable
//
//        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        SingleOutputStreamOperator<TrackerEvent> trackerEventSingleOutputStreamOperator = env
//                .fromElements(events.toArray(new TrackerEvent[events.size()]))
//                .assignTimestampsAndWatermarks(
//                        getTimestampAndWatermarkAssigner(30)
//                );
////        trackerEventSingleOutputStreamOperator
//
//        KeyedStream<TrackerEvent, String> streamByTrackerId = trackerEventSingleOutputStreamOperator
//                .keyBy(TrackerEvent::getTrackerId);
//
//        SingleOutputStreamOperator<EventTrigger> processParkingTrigger = streamByTrackerId
//                .timeWindow(Time.seconds(300), Time.seconds(TriggerParam.PARKING_TIMEOUT_EVALUATION_PERIOD_SECONDS))
//                .process(new ProcessParkingByTimeWindow(parameterTool));
//        processParkingTrigger.addSink(new PrintSinkFunction(false));
//        processParkingTrigger.print("PARKING TimeWindow >>>");
//        env.execute();
//
//
//    }
}
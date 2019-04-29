package ua.kernel.dabbd.triggers.functions;

import org.apache.flink.api.java.utils.ParameterTool;
import org.junit.Before;
import org.junit.Test;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.triggers.functions.util.TestCollector;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProcessDataGapTest {
    private static final int DEFAULT_DATA_GAP_TIMEGAP_SECONDS = 3 * 60;
    private static final int DEFAULT_DATA_GAP_DISTANCE_METERS = 500;
    private static final int DEFAULT_DATA_GAP_SPEED_KMH = 10;
    String[] args = {"--test"};
    ParameterTool parameterTool = ParameterTool.fromArgs(args);

    private ProcessDataGap sut = new ProcessDataGap(parameterTool);
    private static final TrackerEvent TRACKER_EVENT_1 = new TrackerEvent();
    private static final TrackerEvent TRACKER_EVENT_2 = new TrackerEvent();
    private static final LocalDateTime TEST_TIME = LocalDateTime.now();

    @Before
    public void setup() {
        // Events that will cause the trigger
        TRACKER_EVENT_1.setEventDt(TEST_TIME);
        TRACKER_EVENT_1.setSpeed(11);
        TRACKER_EVENT_1.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});

        TRACKER_EVENT_2.setEventDt(TEST_TIME.plusSeconds(DEFAULT_DATA_GAP_TIMEGAP_SECONDS + 1));
        TRACKER_EVENT_2.setSpeed(11);
        TRACKER_EVENT_2.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.52);
        }});
    }

    @Test
    public void testProcessFunction_SmallTimeGap() {
        TRACKER_EVENT_2.setEventDt(TEST_TIME.plusSeconds(DEFAULT_DATA_GAP_TIMEGAP_SECONDS - 1));

        List<TrackerEvent> events = Arrays.asList(TRACKER_EVENT_1, TRACKER_EVENT_2);

        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());

        sut.process("key1", null, events, out);

        assertThat(out.list.size(), is(0));

    }

    @Test
    public void testProcessFunction_SmallDistance() {
        TRACKER_EVENT_2.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.511);
        }});

        List<TrackerEvent> events = Arrays.asList(TRACKER_EVENT_1, TRACKER_EVENT_2);

        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());

        sut.process("key1", null, events, out);

        assertThat(out.list.size(), is(0));

    }

    @Test
    public void testProcessFunction_SmallSpeed() {
        TRACKER_EVENT_2.setSpeed(DEFAULT_DATA_GAP_SPEED_KMH - 1);

        List<TrackerEvent> events = Arrays.asList(TRACKER_EVENT_1, TRACKER_EVENT_2);

        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());

        sut.process("key1", null, events, out);

        assertThat(out.list.size(), is(0));

    }

    @Test
    public void testProcessFunction_emitTrigger() {

        List<TrackerEvent> events = Arrays.asList(TRACKER_EVENT_1, TRACKER_EVENT_2);

        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        sut.process("key1", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(1));

    }


}
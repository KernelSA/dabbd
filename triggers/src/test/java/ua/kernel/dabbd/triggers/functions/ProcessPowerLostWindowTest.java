package ua.kernel.dabbd.triggers.functions;

import org.junit.Before;
import org.junit.Test;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.triggers.functions.util.TestCollector;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProcessPowerLostWindowTest {

    private static final LocalDateTime TEST_TIME = LocalDateTime.now();

    private ProcessPowerLostWindow sut = new ProcessPowerLostWindow();
    private ArrayList<TrackerEvent> events;


    @Before
    public void setup() {
        events = new ArrayList<>();
        // Events that will cause the trigger
        TrackerEvent trackerEvent = new TrackerEvent();
        trackerEvent.setPowerLevel(12);
        trackerEvent.setEventDt(TEST_TIME);
        trackerEvent.setSpeed(11);
        trackerEvent.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});
        events.add(trackerEvent);

        TrackerEvent trackerEvent1 = new TrackerEvent();
        trackerEvent1.setPowerLevel(12);
        trackerEvent1.setEventDt(TEST_TIME);
        trackerEvent1.setSpeed(11);
        trackerEvent1.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});
        events.add(trackerEvent1);

        TrackerEvent trackerEvent2 = new TrackerEvent();
        trackerEvent2.setPowerLevel(0);
        trackerEvent2.setEventDt(TEST_TIME);
        trackerEvent2.setSpeed(11);
        trackerEvent2.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});
        events.add(trackerEvent2);

    }

    @Test
    public void testProcessPowerLostWindow_triggered() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        sut.process("", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(1));

    }

    @Test
    public void testProcessPowerLostWindow_noTrigger_noZeroPower() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        events.get(2).setPowerLevel(10);
        sut.process("", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(0));

    }

    @Test
    public void testProcessPowerLostWindow_noTrigger_speedIsZero() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        events.get(2).setSpeed(0);
        sut.process("", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(0));

    }


    @Test
    public void testProcessPowerLostWindow_noTrigger_notAllPrevPowerssArePresent() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        events.get(1).setPowerLevel(0);
        sut.process("", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(0));

    }


}
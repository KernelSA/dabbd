package ua.kernel.dabbd.triggers.functions;

import org.junit.Before;
import org.junit.Test;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.triggers.functions.util.TestCollector;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProcessFuelLevelTest {

    private static final LocalDateTime TEST_TIME = LocalDateTime.now();
    private static final int DEFAULT_FUEL_LEVEL_SPIKE = 10;

    private ProcessFuelLevel sut = new ProcessFuelLevel(DEFAULT_FUEL_LEVEL_SPIKE);

    private ArrayList<TrackerEvent> events;


    @Before
    public void setup() {
        events = new ArrayList<>();
        // Events that will cause the trigger
        TrackerEvent trackerEvent = new TrackerEvent();
        trackerEvent.setFuelLevel(115);
        trackerEvent.setEventDt(TEST_TIME);
        trackerEvent.setSpeed(11);
        trackerEvent.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});

        TrackerEvent trackerEvent1 = new TrackerEvent();
        trackerEvent1.setFuelLevel(100);
        trackerEvent1.setEventDt(TEST_TIME);
        trackerEvent1.setSpeed(11);
        trackerEvent1.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});

        TrackerEvent trackerEvent2 = new TrackerEvent();
        trackerEvent2.setFuelLevel(100);
        trackerEvent2.setEventDt(TEST_TIME);
        trackerEvent2.setSpeed(11);
        trackerEvent2.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});

        TrackerEvent trackerEvent3 = new TrackerEvent();
        trackerEvent3.setFuelLevel(100);
        trackerEvent3.setEventDt(TEST_TIME);
        trackerEvent3.setSpeed(11);
        trackerEvent3.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});

        TrackerEvent trackerEvent4 = new TrackerEvent();
        trackerEvent4.setFuelLevel(100);
        trackerEvent4.setEventDt(TEST_TIME);
        trackerEvent4.setSpeed(11);
        trackerEvent4.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});

        TrackerEvent trackerEvent5 = new TrackerEvent();
        trackerEvent5.setFuelLevel(100);
        trackerEvent5.setEventDt(TEST_TIME);
        trackerEvent5.setSpeed(11);
        trackerEvent5.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});

        events.add(trackerEvent);
        events.add(trackerEvent1);
        events.add(trackerEvent2);
        events.add(trackerEvent3);
        events.add(trackerEvent4);
        events.add(trackerEvent5);

    }

    @Test
    public void testProcessFuelLevel_LevelDrop() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        sut.process("", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(1));

        assertThat(out.list.get(0).getTriggerInfo(), containsString("DROPPED"));
    }

    @Test
    public void testProcessFuelLevel_LevelJump() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        events.get(0).setFuelLevel(80);
        sut.process("", null, events, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(1));
        assertThat(out.list.get(0).getTriggerInfo(), containsString("JUMPED"));

    }

    @Test
    public void testProcessFuelLevel_TriggerNotFired() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        // Fuel deviation not enough to fire the Trigger
        events.get(0).setFuelLevel(105);
        sut.process("", null, events, out);

        assertThat(out.list.size(), is(0));

    }

    @Test
    public void testProcessFuelLevel_TriggerNotFired2() {
        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        // Fuel deviation not enough to fire the Trigger
        events.get(4).setFuelLevel(105);
        sut.process("", null, events, out);

        assertThat(out.list.size(), is(0));

    }
}
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
import static org.junit.Assert.*;

public class ProcessPowerLostTest {
    private static final LocalDateTime TEST_TIME = LocalDateTime.now();

    private ProcessPowerLost sut = new ProcessPowerLost();


    @Test
    public void testProcessPowerLost() {

        TestCollector<EventTrigger> out = new TestCollector<>(new ArrayList<>());
        TrackerEvent trackerEvent = new TrackerEvent();
        trackerEvent.setFuelLevel(111);
        trackerEvent.setEventDt(TEST_TIME);
        trackerEvent.setSpeed(11);
        trackerEvent.setPowerLevel(0);
        trackerEvent.setCoordinates(new ArrayList<Double>() {{
            add(50.07);
            add(31.51);
        }});
        sut.processElement(trackerEvent, null, out);

        System.out.println(out.list);

        assertThat(out.list.size(), is(1));

    }


}
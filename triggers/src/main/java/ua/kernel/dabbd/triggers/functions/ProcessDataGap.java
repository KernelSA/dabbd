package ua.kernel.dabbd.triggers.functions;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.util.DabBdUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.TRACKER_DATA_GAP;

@Slf4j
public class ProcessDataGap extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {

    private long timeGap;
    private int distanceGap;
    private int speed;

    public ProcessDataGap(long timeGap, int distanceGap, int speed) {
        this.timeGap = timeGap;
        this.distanceGap = distanceGap;
        this.speed = speed;
    }

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        if (events.size() != 2) {
            log.warn("For data-gap trigger window should contain 2 elements");
            return;
        }

        TrackerEvent trackerEvent1 = events.get(0);
        TrackerEvent trackerEvent2 = events.get(1);

        Duration duration = Duration.between(trackerEvent1.getEventDt(), trackerEvent2.getEventDt());
        if (duration.getSeconds() < timeGap)
            return;

        if (trackerEvent1.getSpeed() < speed || trackerEvent2.getSpeed() < speed)
            return;

        ArrayList<Double> coordinates1 = trackerEvent1.getCoordinates();
        ArrayList<Double> coordinates2 = trackerEvent2.getCoordinates();
        if (coordinates1 == null || coordinates2 == null) {
            log.warn("For trigger TRACKER_DATA_GAP both events should contain coordinates. Events: {}", events);
            return;
        }

        double distanceM = DabBdUtils.distance(coordinates1.get(0), coordinates1.get(1), coordinates2.get(0), coordinates2.get(1), 'K') * 1000;
        if (distanceM < distanceGap)
            return;


        EventTrigger eventTrigger = new EventTrigger();
        eventTrigger.setTrackerId(key);
        eventTrigger.setTriggerDt(LocalDateTime.now());
        eventTrigger.setTriggerInfo("Time gap: " + duration.getSeconds() + " s, speed1: "
                + trackerEvent1.getSpeed() + " km/h, speed2: "
                + trackerEvent2.getSpeed() + " km/h, distance: "
                + distanceM + " m.");
        eventTrigger.setTriggerEvents(events);
        eventTrigger.setTriggerType(TRACKER_DATA_GAP);
        eventTrigger.setEventDt(trackerEvent2.getEventDt());

        out.collect(eventTrigger);

    }


}



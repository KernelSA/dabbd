package ua.kernel.dabbd.triggers.functions;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.PurgingTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.SIGNAL_LOST;

public class ProcessSignalLost extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {

    private int lostTrackerTimeoutSeconds;
    private int lostTrackerSpeedThreshold;
    private int lostTrackerPowerThreshold;

    public ProcessSignalLost(int lostTrackerTimeoutSeconds, int lostTrackerSpeedThreshold, int lostTrackerPowerThreshold) {
        this.lostTrackerTimeoutSeconds = lostTrackerTimeoutSeconds;
        this.lostTrackerSpeedThreshold = lostTrackerSpeedThreshold;
        this.lostTrackerPowerThreshold = lostTrackerPowerThreshold;
    }

    public static PurgingTrigger<Object, GlobalWindow> processTimeLostTrackerTrigger(int evaluationPeriod) {
        return PurgingTrigger.of(ContinuousProcessingTimeTrigger.of(Time.seconds(evaluationPeriod)));
    }

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        LocalDateTime processingDt = LocalDateTime.now();
        LocalDateTime timeoutDt = processingDt.minusSeconds(lostTrackerTimeoutSeconds);

        if (events.stream().map(TrackerEvent::getEventDt).allMatch(eventDt -> eventDt.isBefore(timeoutDt))) {
            TrackerEvent lastTrackerEvent = events.get(events.size() - 1);
            if (lastTrackerEvent.getSpeed() > lostTrackerSpeedThreshold && lastTrackerEvent.getPowerLevel() > lostTrackerPowerThreshold) {
                EventTrigger eventTrigger = new EventTrigger();
                eventTrigger.setTrackerId(key);
                eventTrigger.setTriggerDt(processingDt);

                LocalDateTime lastEventDt = lastTrackerEvent.getEventDt();
                eventTrigger.setTriggerInfo("Last EventDt: " + lastEventDt + ", processingDt: " + processingDt);
                eventTrigger.setTriggerEvents(events);
                eventTrigger.setTriggerType(SIGNAL_LOST);
                eventTrigger.setEventDt(lastEventDt);
                out.collect(eventTrigger);
            }
        }
    }


}

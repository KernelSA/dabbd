package ua.kernel.dabbd.triggers.functions;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.PurgingTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.triggers.config.TriggerParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.SIGNAL_LOST;

public class ProcessSignalLost extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {

    private int lostTrackerTimeoutSeconds;
    private int lostTrackerSpeedThreshold;
    private int lostTrackerPowerThreshold;

    public ProcessSignalLost(ParameterTool parameterTool) {
        this.lostTrackerTimeoutSeconds = parameterTool.getInt(TriggerParam.LOST_TRACKER_TIMEOUT_SECONDS.key(), TriggerParam.LOST_TRACKER_TIMEOUT_SECONDS.defaultValue());
        this.lostTrackerSpeedThreshold = parameterTool.getInt(TriggerParam.LOST_TRACKER_SPEED_THRESHOLD.key(), TriggerParam.LOST_TRACKER_SPEED_THRESHOLD.defaultValue());
        this.lostTrackerPowerThreshold = parameterTool.getInt(TriggerParam.LOST_TRACKER_POWER_THRESHOLD.key(), TriggerParam.LOST_TRACKER_POWER_THRESHOLD.defaultValue());
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

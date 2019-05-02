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
import java.util.Arrays;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.PARKING;

public class ProcessParkingByTimeout extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {

    private int trackerStoppedTimeoutSeconds;
    private int trackerStoppedSpeedThreshold;

    public ProcessParkingByTimeout(ParameterTool parameterTool) {
        this.trackerStoppedTimeoutSeconds = parameterTool.getInt(TriggerParam.PARKING_TIME_WINDOW_SECONDS.key(), TriggerParam.PARKING_TIME_WINDOW_SECONDS.defaultValue());
        this.trackerStoppedSpeedThreshold = parameterTool.getInt(TriggerParam.PARKING_SPEED_THRESHOLD_KMH.key(), TriggerParam.PARKING_SPEED_THRESHOLD_KMH.defaultValue());
    }

    public static PurgingTrigger<Object, GlobalWindow> processTimeoutParkingTrigger(int evaluationPeriod) {
        return PurgingTrigger.of(ContinuousProcessingTimeTrigger.of(Time.seconds(evaluationPeriod)));
    }

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        LocalDateTime processingDt = LocalDateTime.now();
        LocalDateTime timeoutDt = processingDt.minusSeconds(trackerStoppedTimeoutSeconds);

        if (events.stream().map(TrackerEvent::getEventDt).allMatch(eventDt -> eventDt.isBefore(timeoutDt))) {
            TrackerEvent lastTrackerEvent = events.get(events.size() - 1);
            if (lastTrackerEvent.getSpeed() <= trackerStoppedSpeedThreshold) {
                EventTrigger eventTrigger = new EventTrigger();
                eventTrigger.setTrackerId(key);
                eventTrigger.setTriggerDt(processingDt);

                LocalDateTime lastEventDt = lastTrackerEvent.getEventDt();
                eventTrigger.setTriggerInfo("Tracker STOPPED and signal lost. Last EventDt: " + lastEventDt
                        + ", processingDt: " + processingDt +". Events in window: " + events.size());
                eventTrigger.setTriggerEvents(Arrays.asList(events.get(0),lastTrackerEvent));
                eventTrigger.setTriggerType(PARKING);
                eventTrigger.setEventDt(lastEventDt);
                out.collect(eventTrigger);
            }
        }
    }


}

package ua.kernel.dabbd.triggers.functions;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.PurgingTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.util.DabBdUtils;
import ua.kernel.dabbd.triggers.config.TriggerParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.PARKING;

public class ProcessParkingByTimeWindow extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, TimeWindow> {


    private int stopTrackerTimeoutSeconds;
    private int stopTrackerSpeedThreshold;
    private int stopTrackerDistanceThreshold;

    public ProcessParkingByTimeWindow(ParameterTool parameterTool) {
        this.stopTrackerTimeoutSeconds = parameterTool.getInt(TriggerParam.PARKING_TIME_WINDOW_SECONDS.key(), TriggerParam.PARKING_TIME_WINDOW_SECONDS.defaultValue());
        this.stopTrackerSpeedThreshold = parameterTool.getInt(TriggerParam.PARKING_SPEED_THRESHOLD_KMH.key(), TriggerParam.PARKING_SPEED_THRESHOLD_KMH.defaultValue());
        this.stopTrackerDistanceThreshold = parameterTool.getInt(TriggerParam.PARKING_DISTANCE_THRESHOLD_METERS.key(), TriggerParam.PARKING_DISTANCE_THRESHOLD_METERS.defaultValue());

    }

    public static PurgingTrigger<Object, GlobalWindow> processStopTrackerTrigger(int evaluationPeriod) {
        return PurgingTrigger.of(ContinuousProcessingTimeTrigger.of(Time.seconds(evaluationPeriod)));
    }

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        LocalDateTime processingDt = LocalDateTime.now();

        double avgSpeed = events.stream().mapToInt(TrackerEvent::getSpeed).average().orElse(0);
        TrackerEvent firstEvent = events.get(0);
        double maxDistanceM = events.stream().mapToDouble(evt -> DabBdUtils.distance(
                firstEvent.getCoordinates().get(0), firstEvent.getCoordinates().get(1),
                evt.getCoordinates().get(0), evt.getCoordinates().get(1), 'K') * 1000
        ).max().orElse(0);

        if (avgSpeed <= stopTrackerSpeedThreshold && maxDistanceM <= stopTrackerDistanceThreshold) {

            EventTrigger eventTrigger = new EventTrigger();
            eventTrigger.setTrackerId(key);
            eventTrigger.setTriggerDt(processingDt);

            LocalDateTime lastEventDt = events.get(events.size() - 1).getEventDt();
            eventTrigger.setTriggerInfo("Last EventDt: " + lastEventDt + ", avgSpeed: " + avgSpeed + ", maxDistanceM: " + maxDistanceM);
            eventTrigger.setTriggerEvents(Arrays.asList(firstEvent, events.get(events.size()-1)));
            eventTrigger.setTriggerType(PARKING);
            eventTrigger.setEventDt(lastEventDt);
            out.collect(eventTrigger);

        }

    }
}



package ua.kernel.dabbd.triggers.functions;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.triggers.config.TriggerParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.POWER_LOST;

@Slf4j
public class ProcessPowerLostWindow extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {

    private int zeroPowerCount;
    private int speedLimit;

    public ProcessPowerLostWindow(ParameterTool parameterTool) {
        this.zeroPowerCount = parameterTool.getInt(TriggerParam.POWER_LOST_ZERO_POWER_COUNT.key(), TriggerParam.POWER_LOST_ZERO_POWER_COUNT.defaultValue());
        this.speedLimit = parameterTool.getInt(TriggerParam.POWER_LOST_SPEED_LIMIT.key(), TriggerParam.POWER_LOST_SPEED_LIMIT.defaultValue());
    }

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        if (events.size() < 3 && events.size() < zeroPowerCount) {
            log.warn("POWER_LOST trigger - window should contain more then 2 elements and zeroPowerCount less then window size. Actual count of events in window {}", events.size());
            return;
        }

        if (events.stream().anyMatch(trackerEvent -> trackerEvent.getSpeed() <= speedLimit)) {
            // not all events have speed > 2 km/h
            return;
        }


        boolean aggPositive = true;
        for (int i = 0; i < events.size() - zeroPowerCount; i++) {
            aggPositive &= events.get(i).getPowerLevel() > 0;
        }
        boolean aggNegative = true;
        for (int i = events.size() - zeroPowerCount; i < events.size(); i++) {
            aggNegative &= events.get(i).getPowerLevel() <= 0;
        }

        if (aggPositive && aggNegative) {
            EventTrigger eventTrigger = new EventTrigger();
            eventTrigger.setTrackerId(key);
            eventTrigger.setTriggerDt(LocalDateTime.now());
            eventTrigger.setTriggerInfo("Power greater then zero for " + (events.size() - zeroPowerCount) + " and zero for " + zeroPowerCount + " events");
            eventTrigger.setTriggerEvents(events);
            eventTrigger.setTriggerType(POWER_LOST);

            eventTrigger.setEventDt(events.get(events.size() - 1).getEventDt());
            out.collect(eventTrigger);
        }

    }


}



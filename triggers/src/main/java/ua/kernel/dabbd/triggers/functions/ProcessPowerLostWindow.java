package ua.kernel.dabbd.triggers.functions;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.POWER_LOST;

@Slf4j
public class ProcessPowerLostWindow extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        if (events.size() < 2) {
            log.warn("For POWER_LOST trigger - window should contain more then 2 elements");
            return;
        }
        boolean agg = true;
        for (int i = 0; i < events.size() - 1; i++) {
            agg &= events.get(i).getPowerLevel() > 0;
        }
        TrackerEvent value = events.get(events.size() - 1);

        if (value.getSpeed() > 0 && value.getPowerLevel() <= 0 && agg) {
            EventTrigger eventTrigger = new EventTrigger();
            eventTrigger.setTrackerId(value.getTrackerId());
            eventTrigger.setTriggerDt(LocalDateTime.now());
            eventTrigger.setTriggerInfo("Speed: " + value.getSpeed() + " km/h, powerLevel: " + value.getPowerLevel());
            eventTrigger.setTriggerEvents(events);
            eventTrigger.setTriggerType(POWER_LOST);

            eventTrigger.setEventDt(value.getEventDt());
            out.collect(eventTrigger);
        }

    }


}



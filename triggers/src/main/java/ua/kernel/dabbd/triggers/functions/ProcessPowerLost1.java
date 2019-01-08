package ua.kernel.dabbd.triggers.functions;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;

import java.time.LocalDateTime;
import java.util.Collections;

import static ua.kernel.dabbd.commons.model.TriggerType.POWER_LOST;

public class ProcessPowerLost1 extends KeyedProcessFunction<String, TrackerEvent, EventTrigger> {

    @Override
    public void processElement(TrackerEvent value, Context ctx, Collector<EventTrigger> out) {
        if (value.getSpeed() > 0 && value.getPowerLevel() <= 0) {
            EventTrigger eventTrigger = new EventTrigger();
            eventTrigger.setTrackerId(value.getTrackerId());
            eventTrigger.setTriggerDt(LocalDateTime.now());
            eventTrigger.setTriggerInfo("Speed: " + value.getSpeed() + " km/h, powerLevel: " + value.getPowerLevel());
            eventTrigger.setTriggerEvents(Collections.singletonList(value));
            eventTrigger.setTriggerType(POWER_LOST);

            eventTrigger.setEventDt(value.getEventDt());
            out.collect(eventTrigger);
        }
    }

}

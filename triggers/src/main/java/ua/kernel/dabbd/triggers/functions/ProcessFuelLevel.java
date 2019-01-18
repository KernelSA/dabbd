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

import static ua.kernel.dabbd.commons.model.TriggerType.FUEL_LEVEL_JUMP;

@Slf4j
public class ProcessFuelLevel extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {
    private int fuelLevelSpike;

    public ProcessFuelLevel(int fuelLevelSpike) {
        this.fuelLevelSpike = fuelLevelSpike;
    }

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        if (events.size() < 3) {
            log.warn("For FUEL_LEVEL_JUMP trigger - window should contain more then 3 elements");
            return;
        }

        TrackerEvent trackerEvent1 = events.get(0);
        Integer fuelLevel1 = trackerEvent1.getFuelLevel();
        double fuelBaseLevel = (fuelLevel1 > 0 ? fuelLevel1 : 1) * 1.0;
        ArrayList<Integer> agg = new ArrayList<>();
        for (int i = 1; i < events.size(); i++) {
            TrackerEvent trackerEvent = events.get(i);
            Integer fuelLevelN = trackerEvent.getFuelLevel();
            int diff = fuelLevel1 - fuelLevelN;
            agg.add((Math.abs(diff / fuelBaseLevel) * 100 < fuelLevelSpike) ? 0 : Integer.signum(diff));
        }

        if (agg.stream().anyMatch(d -> d == 0)) {
            // not all 5 levels had a diff > N% -> not match trigger definition
            return;
        }
        int fuelAggregate = agg.stream().mapToInt(value -> value).sum();
        if (Math.abs(fuelAggregate) != events.size() - 1) {
            // there was + and - spikes ing fuel level -> not match trigger definition
            return;
        }

        EventTrigger eventTrigger = new EventTrigger();
        eventTrigger.setTrackerId(key);
        eventTrigger.setTriggerDt(LocalDateTime.now());
        eventTrigger.setTriggerInfo("Fuel level " + (fuelAggregate > 0 ? "DROPPED" : "JUMPED"));
        eventTrigger.setTriggerEvents(events);
        eventTrigger.setTriggerType(FUEL_LEVEL_JUMP);
        eventTrigger.setEventDt(trackerEvent1.getEventDt());

        out.collect(eventTrigger);

    }

}

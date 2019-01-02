package ua.kernel.dabbd.triggers.functions;

import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.PurgingTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ua.kernel.dabbd.commons.model.TriggerType.SIGNAL_LOST;

public class ProcessSignalLost extends ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> {

    private static final ZoneOffset OFFSET = OffsetDateTime.now().getOffset();

    private int lostTrackerTimeoutSeconds;
    private BoundedOutOfOrdernessTimestampExtractor<TrackerEvent> timestampAndWatermarkAssigner;

    public ProcessSignalLost(int lostTrackerTimeoutSeconds, BoundedOutOfOrdernessTimestampExtractor<TrackerEvent> timestampAndWatermarkAssigner) {
        this.lostTrackerTimeoutSeconds = lostTrackerTimeoutSeconds;
        this.timestampAndWatermarkAssigner = timestampAndWatermarkAssigner;
    }

    public static PurgingTrigger<Object, GlobalWindow> processTimeLostTrackerTrigger(int evaluationPeriod) {
        return PurgingTrigger.of(ContinuousProcessingTimeTrigger.of(Time.seconds(evaluationPeriod)));
    }

    @Override
    public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {

        List<TrackerEvent> events = new ArrayList<>();
        elements.forEach(events::add);

        // Zero or 1 event in lostTrackerTimeoutSeconds = SIGNAL_LOST event
        if (events.size() <= 1) {
            LocalDateTime eventDt = events.get(0).getEventDt();
            LocalDateTime processingDt = LocalDateTime.now();
            if (eventDt.minusSeconds(lostTrackerTimeoutSeconds)
                    .isBefore(processingDt)) {

                EventTrigger eventTrigger = new EventTrigger();
                eventTrigger.setTrackerId(key);
                eventTrigger.setTriggerDt(processingDt);
                String contextInfo = ", context: ";
                if (context != null) {
                    LocalDateTime contextProcessingDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(context.currentProcessingTime()), OFFSET);
                    LocalDateTime contextWatermarkDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(context.currentWatermark()), OFFSET);
                    contextInfo += " contextProcessingDt(" + contextProcessingDt + "), contextWatermarkDt(" + contextWatermarkDt + ")";
                }
                LocalDateTime assignerCurrentWatermarkDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampAndWatermarkAssigner.getCurrentWatermark().getTimestamp()), OFFSET);
                eventTrigger.setTriggerInfo("EventDt: " + eventDt
                        + ", processingDt: " + processingDt+ contextInfo
                        + ", timestampFromAssigner: " + assignerCurrentWatermarkDt);
                eventTrigger.setTriggerEvents(events);
                eventTrigger.setTriggerType(SIGNAL_LOST);
                eventTrigger.setEventDt(eventDt);
                out.collect(eventTrigger);
            }
        }
    }


}

package ua.kernel.dabbd.eventlistener.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.commons.entity.EventsEntity;
import ua.kernel.dabbd.commons.entity.TriggerLogEntity;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.repository.EventsRepository;
import ua.kernel.dabbd.commons.repository.TriggerLogRepository;
import ua.kernel.dabbd.eventlistener.config.EventListenerConfig;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Receiver {

    private final EventListenerConfig config;
    private final EventsRepository eventsRepository;
    private final TriggerLogRepository triggerLogRepository;

    @PostConstruct
    public void init() {
        log.info("=> Receiver init");
    }

    @KafkaListener(topics = "${kernel.dabbd.listener.events-topic}",
            containerFactory = "trackerEventKafkaListenerContainerFactory")
    public void listenTrackerEvent(@Payload TrackerEvent message, @Headers Map<String, Object> headers) {
        if (log.isTraceEnabled()) {
            log.trace("=>> msg TrackerEvent: {}, with headers: '{}'", message, headers);
        }
        if (message == null) {
            return;
        }
        LocalDateTime kafkaReceivedDt = null;
        try {
            kafkaReceivedDt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli((Long) headers.get("kafka_receivedTimestamp")),
                    TimeZone.getDefault().toZoneId());
        } catch (Exception e) {
            log.warn("Can't parse DateTime from 'kafka_receivedTimestamp' header", e);
        }

        EventsEntity eventEntity = EventsEntity.builder()
                .trackerId(message.getTrackerId())
                .sourceType(message.getSourceType())
                .eventDt(message.getEventDt())
                .latitude(message.getCoordinates().get(0))
                .longitude(message.getCoordinates().get(1))
                .speed(message.getSpeed())
                .fuel(message.getFuelLevel())
                .power(message.getPowerLevel())
                .gsmSignal(message.getGsmSignal())
                .gpsSatellites(message.getSattelites())
                .kafkaTimestamp(kafkaReceivedDt)
                .build();
        eventsRepository.save(eventEntity);

    }

    @KafkaListener(topics = "${kernel.dabbd.listener.triggers-topic}",
            containerFactory = "eventTriggerKafkaListenerContainerFactory")
    public void listenString(@Payload EventTrigger message, @Headers Map<String, Object> headers) {
        if (log.isTraceEnabled()) {
            log.trace("=>> EventTrigger:  {}, with headers: '{}'", message, headers);
        }
        if (message == null) {
            return;
        }

        TriggerLogEntity.TriggerLogEntityBuilder triggerLogEntityBuilder = TriggerLogEntity.builder()
                .trackerId(message.getTrackerId())
                .triggerType(message.getTriggerType().name())
                .triggerDt(message.getTriggerDt())
                .eventDt(message.getEventDt());

        if (config.getWriteEventsToTriggerLog()) {
            triggerLogEntityBuilder
                    .triggerInfo("info: " + message.getTriggerInfo() + " events: " + message.getTriggerEvents().toString());
        } else {
            triggerLogEntityBuilder
                    .triggerInfo(message.getTriggerInfo());
        }

        triggerLogRepository.save(triggerLogEntityBuilder.build());

    }


/*    @KafkaListener(
            topics = "${kernel.dabbd.listener.topic}",
            containerFactory = "stringKafkaListenerContainerFactory")
    public void listenString(@Payload String message) {
        if (log.isTraceEnabled()) {
            log.trace("=>> msg String: {}", message);
        }
//        Iterable<EventsEntity> all = eventsRepository.findAll();
//        all.forEach(eventsEntity -> log.info("Event from DB: {}", eventsEntity));

    }*/
}
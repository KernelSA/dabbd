package ua.kernel.dabbd.eventlistener.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.commons.entity.EventsEntity;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.repository.EventsRepository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Receiver {

    private final EventsRepository eventsRepository;

    static {
        System.out.println("=>>> Receiver");
    }

    @PostConstruct
    public void init() {
        log.info("=> Receiver PostConstruct");
    }

    @KafkaListener(
            topics = "${kernel.dabbd.listener.topic}",
            containerFactory = "trackerEventKafkaListenerContainerFactory"
//            , groupId = "${kernel.dabbd.listener.group.id}"
    )
    public void listenTrackerEvent(@Payload TrackerEvent message, @Headers Map<String,Object> headers) {
        if (log.isTraceEnabled()) {
            log.trace("=>> msg TrackerEvent: {}, with headers: '{}'", message, headers);
        }
        if (message == null) {
            return;
        }
//        Object kafka_receivedTimestamp = headers.get("kafka_receivedTimestamp");
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
//                .kafkaTimestamp(kafka_receivedTimestamp)
                .build();
        eventsRepository.save(eventEntity);
//        Iterable<EventsEntity> all = eventsRepository.findAll();
//        all.forEach(eventsEntity -> log.info("Event from DB: {}", eventsEntity));

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
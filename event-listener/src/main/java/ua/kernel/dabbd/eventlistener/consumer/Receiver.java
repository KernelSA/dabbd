package ua.kernel.dabbd.eventlistener.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.commons.entity.EventsEntity;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.repository.EventsRepository;

import javax.annotation.PostConstruct;

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

    @KafkaListener(topics = "${kernel.dabbd.listener.topic}")
    public void listen(@Payload TrackerEvent message) {

        log.info("msg =>>  {}", message);

        Iterable<EventsEntity> all = eventsRepository.findAll();
        all.forEach(eventsEntity -> log.info("Event from DB: {}", eventsEntity));

    }
}
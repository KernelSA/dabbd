package ua.kernel.dabbd.eventlistener.consumer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class Receiver {

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @PostConstruct
    public void init() {
        log.info("=> Receiver PostConstruct");
    }

    @KafkaListener(topics = "${kernel.dabbd.listener.topic}")
    public void listen(@Payload String message) {

        LOG.info("msg =>> " + message);

    }
}
package ua.kernel.dabbd.tracking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.model.WaybillRequest;
import ua.kernel.dabbd.tracking.config.RouteTrackingProperties;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Receiver {

    private final RouteTrackingProperties routeTrackingProperties;

    @PostConstruct
    public void init() {
        log.info("=> Receiver init");
    }
/*
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


    }*/

    @KafkaListener(topics = "#{routeTrackingProperties.waybillTopic}", groupId = "#{routeTrackingProperties.waybillConsumerGroup}",
            containerFactory = "eventTriggerKafkaListenerContainerFactory")
    public void listenString(@Payload WaybillRequest[] message, @Headers Map<String, Object> headers) {
        if (log.isTraceEnabled()) {
            log.trace("=>> EventTrigger:  {}, with headers: '{}'", message, headers);
        }
        if (message == null) {
            return;
        }
        log.info("=>> EventTrigger:  {}, with headers: '{}'", message[0], headers);

    }

}
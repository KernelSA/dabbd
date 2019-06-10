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

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Receiver {

    private final TrackerService trackerService;

    @PostConstruct
    public void init() {
        log.info("=> Receiver init");
    }

    @KafkaListener(
            topics = "#{routeTrackingProperties.eventsTopic}",
            groupId = "#{routeTrackingProperties.eventsConsumerGroup}",
            containerFactory = "trackerEventKafkaListenerContainerFactory")
    public void listenTrackerEvent(@Payload TrackerEvent message, @Headers Map<String, Object> headers) {
        if (log.isTraceEnabled()) {
            log.trace("=>> msg TrackerEvent: {}, with headers: '{}'", message, headers);
        }
        if (message == null) {
            return;
        }

        trackerService.trackEvent(message);

    }

    @KafkaListener(
            topics = "#{routeTrackingProperties.waybillTopic}",
            groupId = "#{routeTrackingProperties.waybillConsumerGroup}",
            containerFactory = "waybillKafkaListenerContainerFactory")
    public void listenString(@Payload WaybillRequest[] message, @Headers Map<String, Object> headers) {
        if (log.isTraceEnabled()) {
            log.trace("=>> EventTrigger:  {}, with headers: '{}'", message, headers);
        }
        if (message == null) {
            return;
        }
        log.info("=>> EventTrigger:  {}, with headers: '{}'", message[0], headers);

        trackerService.addWaybill(message[0]);
    }

}
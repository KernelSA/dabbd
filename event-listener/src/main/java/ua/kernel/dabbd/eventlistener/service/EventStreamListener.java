package ua.kernel.dabbd.eventlistener.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.eventlistener.config.EventListenerConfig;

@Slf4j
@Service
public class EventStreamListener {

    @Autowired
    private EventListenerConfig config;

    public void init() {


    }


}

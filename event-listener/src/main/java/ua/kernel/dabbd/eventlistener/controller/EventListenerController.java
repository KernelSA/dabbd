package ua.kernel.dabbd.eventlistener.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.kernel.dabbd.eventlistener.config.EventListenerConfig;

@RestController
public class EventListenerController {
    @Autowired
    private EventListenerConfig eventListenerConfig;

    @RequestMapping("/test")
    public String test() {
        System.out.println("=>> " + eventListenerConfig.getEventsTopic());
        return "Done!";
    }
}

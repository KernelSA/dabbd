package ua.kernel.dabbd.eventlistener.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kernel.dabbd.listener")
public class EventListenerConfig {

    private String topic;
    private String triggersTopic;
    private Integer concurrency;

   static  {
        System.out.println("=>>>> EventListenerConfig");
    }

}


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


    private String bootstrapServers;

    private String eventsTopic;
    private String eventsConsumerGroup;
    private String eventsAutoOffsetResetConfig = "earliest";
    private Integer eventsConcurrency;

    private String triggersTopic;
    private String triggersConsumerGroup;
    private String triggersAutoOffsetResetConfig = "earliest";
    private Integer triggersConcurrency;


    static {
        System.out.println("=>>>> EventListenerConfig");
    }

}


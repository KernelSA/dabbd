package ua.kernel.dabbd.eventlistener.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
//@EnableConfigurationProperties({EventListenerConfig.class})
@ConfigurationProperties(prefix = "kernel.dabbd.listener")
public class EventListenerConfig {

    private String topic;
    private String triggersTopic;

   static  {
        System.out.println("=>>>> EventListenerConfig");
    }

}


package ua.kernel.dabbd.eventlistener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kernel.dabbd.listener")
public class EventListenerConfig {
}

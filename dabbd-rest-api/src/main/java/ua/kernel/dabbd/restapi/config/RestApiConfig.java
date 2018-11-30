package ua.kernel.dabbd.restapi.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kernel.dabbd.rest")
public class RestApiConfig {
}

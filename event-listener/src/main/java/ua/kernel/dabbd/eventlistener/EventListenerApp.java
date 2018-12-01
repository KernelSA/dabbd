package ua.kernel.dabbd.eventlistener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static ua.kernel.dabbd.commons.DabBdUtils.logSystemProperties;


@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class EventListenerApp {

    public static void main(String[] args) {
        logSystemProperties();
        SpringApplication app = new SpringApplication(EventListenerApp.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }


}

package ua.kernel.dabbd.eventlistener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import ua.kernel.dabbd.commons.config.RepositoryConfig;
import ua.kernel.dabbd.eventlistener.service.EventStreamListener;

import javax.annotation.PostConstruct;

import static ua.kernel.dabbd.commons.util.DabBdUtils.logSystemProperties;


@Slf4j
@Import({
        RepositoryConfig.class
})
@SpringBootApplication
@EnableConfigurationProperties
public class EventListenerApp {

    @Autowired
    private EventStreamListener eventStreamListener;

    public static void main(String[] args) {
        logSystemProperties();
        SpringApplication app = new SpringApplication(EventListenerApp.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }

    @PostConstruct
    public void run() {
        System.out.println("Spring Kafka Producer and Consumer Example");
        eventStreamListener.init();
    }

}

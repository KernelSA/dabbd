package ua.kernel.dabbd.restapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class RestApiApp {

    public static void main(String[] args) {
        logSystemProperties();
        SpringApplication app = new SpringApplication(RestApiApp.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }


    private static void logSystemProperties() {
        log.info("------------------------------------------");
        log.info("System Properties: ");
        System.getProperties().entrySet()
                .forEach(objectObjectEntry ->
                        log.info(">>>>\t" + objectObjectEntry.getKey() + " : " + objectObjectEntry.getValue()));
        log.info("------------------------------------------");
    }

}
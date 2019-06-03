package ua.kernel.dabbd.tracking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import ua.kernel.dabbd.commons.config.RepositoryConfig;

import static ua.kernel.dabbd.commons.util.DabBdUtils.logSystemProperties;


@Slf4j
@Import({
        RepositoryConfig.class
})
@SpringBootApplication
@EnableConfigurationProperties
public class RouteTrackingApp {

    public static void main(String[] args) {
        logSystemProperties();
        SpringApplication app = new SpringApplication(RouteTrackingApp.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }

}

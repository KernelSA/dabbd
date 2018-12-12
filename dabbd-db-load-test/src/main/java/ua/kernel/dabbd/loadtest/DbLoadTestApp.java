package ua.kernel.dabbd.loadtest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ua.kernel.dabbd.loadtest.db.EventsTestEntity;
import ua.kernel.dabbd.loadtest.db.EventsTestRepository;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@EntityScan("ua.kernel.dabbd.loadtest.*")
@EnableJpaRepositories("ua.kernel.dabbd.loadtest.*")
@SpringBootApplication
public class DbLoadTestApp {

    @Autowired
    private EventsTestRepository eventsRepository;

    private static String[] args;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DbLoadTestApp.class);
        app.run(args);
    }

    @PostConstruct
    public void test() {
        Integer numberToGenerate = 3_000_000;
        if (args != null && args[0] != null) {
            numberToGenerate = Integer.valueOf(args[0]);
        }
        log.info("Going to insert {} events in DB");
        LocalDateTime start = LocalDateTime.now();
        for (int i = 0; i < numberToGenerate; i++) {
            eventsRepository.save(EventsTestEntity.builder()
                    .trackerId(UUID.randomUUID().toString())
                    .eventDt(LocalDateTime.now())
                    .speed(1)
                    .fuel(1)
                    .power(1)
                    .latitude(34.12)
                    .longitude(12.34)
                    .build());
        }
        LocalDateTime end = LocalDateTime.now();
        log.info("{} events inserted in {}", Duration.between(start, end).toString());
    }


}
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
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@EntityScan("ua.kernel.dabbd.loadtest.*")
@EnableJpaRepositories("ua.kernel.dabbd.loadtest.*")
@SpringBootApplication
public class DbLoadTestApp {

    @Autowired
    private EventsTestRepository eventsRepository;

    private static String[] ARGS;

    public static void main(String[] args) {
        if (args != null) {
            Arrays.stream(args).forEach(s -> {

                System.out.println("args=" + s);
                log.info("arg={}", s);
            });
        }
        ARGS = args;

        SpringApplication app = new SpringApplication(DbLoadTestApp.class);
        app.run(args);
    }

    @PostConstruct
    public void test() {
        Integer numberToGenerate = 10_000;
        Integer threads = 3;
        if (ARGS != null && ARGS.length > 0) {
            numberToGenerate = Integer.valueOf(ARGS[0]);
            if (ARGS.length > 1) {
                threads = Integer.valueOf(ARGS[1]);
            }

        }

        log.info("Going to insert {} events in {} threads to DB", numberToGenerate, threads);


        int number = numberToGenerate;

        for (int i = 0; i < threads; i++) {
            int threadNum = i;
            Thread thread = new Thread(() -> {
                log.info("Thread {} started", threadNum);
                LocalDateTime start = LocalDateTime.now();
                for (int j = 0; j < number; j++) {
                    eventsRepository.save(EventsTestEntity.builder()
                            .trackerId(UUID.randomUUID().toString())
                            .eventDt(LocalDateTime.now())
                            .speed(1)
                            .fuel(1)
                            .power(1)
                            .latitude(134.12)
                            .longitude(12.34)
                            .build());
                }

                log.info("Thread {} completed", threadNum);
                LocalDateTime end = LocalDateTime.now();
                log.info("{} events inserted in {}", number, Duration.between(start, end).toString());
            });
            thread.start();
        }
    }


}
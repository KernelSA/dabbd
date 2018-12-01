package ua.kernel.dabbd.eventlistener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PersistenceService {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void check() {
       log.info("=> PersistenceService PostConstruct");

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema='public'");
        maps.forEach(stringObjectMap -> {
            System.out.println("=> ");
            stringObjectMap.forEach((s, o) -> System.out.print(s + ":" + o + ", "));
        });

    }

}

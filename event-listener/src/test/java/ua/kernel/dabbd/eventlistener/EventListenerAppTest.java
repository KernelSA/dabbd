package ua.kernel.dabbd.eventlistener;

import kafka.KafkaTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class EventListenerAppTest {

    KafkaTest kafkaTest;

    @Test
    public void contextLoads() {
    }

}
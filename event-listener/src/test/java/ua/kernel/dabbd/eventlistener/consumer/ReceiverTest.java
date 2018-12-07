package ua.kernel.dabbd.eventlistener.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ua.kernel.dabbd.eventlistener.producer.SpringKafkaSenderTest;
import ua.kernel.dabbd.eventlistener.service.Sender;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertThat;
import static org.springframework.kafka.test.assertj.KafkaConditions.key;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class ReceiverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverTest.class);

    private static String SENDER_TOPIC = "TEST.topic";


    @Autowired
    private Sender sender;

    private KafkaMessageListenerContainer<String, String> container;

    private BlockingQueue<ConsumerRecord<String, String>> records;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka =
            new EmbeddedKafkaRule(1, true, SENDER_TOPIC);

    @Before
    public void setUp() throws Exception {
        // set up the Kafka consumer properties
        Map<String, Object> consumerProperties
                = KafkaTestUtils.consumerProps("sender", "false", embeddedKafka.getEmbeddedKafka());

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProperties);

        // set the topic that needs to be consumed
        ContainerProperties containerProperties = new ContainerProperties(SENDER_TOPIC);

        // create a Kafka MessageListenerContainer
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>();

        // setup a Kafka message listener
        container.setupMessageListener(new MessageListener<String, String>() {
            @Override
            public void onMessage(ConsumerRecord<String, String> record) {
                LOGGER.debug("test-listener received message='{}'", record.toString());
                records.add(record);
            }
        });

        // start the container and underlying message listener
        container.start();

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
    }

    @After
    public void tearDown() {
        // stop the container
        container.stop();
    }

    //    @Test
    public void testSend() throws InterruptedException {
        // send the message
        String greeting = "Hello Spring Kafka Sender!";
        sender.send(greeting);

        // check that the message was received
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        // Hamcrest Matchers to check the value
        assertThat(received, hasValue(greeting));
        // AssertJ Condition to check the key
        Assertions.assertThat(received).has(key(null));
    }

    @Test
    public void testSend2() throws InterruptedException {
        // send the message

        String plainTest = "Hello world!";
        String event = "{\n" +
                "  \"trackerId\": \"353173060726716\",\n" +
                "  \"sourceType\": \"GPSTracker\",\n" +
                "  \"eventDt\": \"27.11.2018-17:33:51\",\n" +
                "  \"coordinates\": [\n" +
                "    49.749393333333337,\n" +
                "    31.978986666666668\n" +
                "  ],\n" +
                "  \"speed\": 0,\n" +
                "  \"fuelLevel\": 0,\n" +
                "  \"powerLevel\": 0,\n" +
                "  \"gsmSignal\": 0,\n" +
                "  \"sattelites\": 16\n" +
                "}\n";

        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);

        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);

        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);

        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);

        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);
        sender.send(plainTest);
        sender.send("");
        sender.send(event);

        // check that the message was received
        Thread.sleep(10000);
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        // Hamcrest Matchers to check the value
        assertThat(received, hasValue(event));
        // AssertJ Condition to check the key
        Assertions.assertThat(received).has(key(null));
    }
}
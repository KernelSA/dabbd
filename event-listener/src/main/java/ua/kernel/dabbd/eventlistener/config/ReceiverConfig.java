package ua.kernel.dabbd.eventlistener.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@EnableKafka
@Configuration
public class ReceiverConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Autowired
    private EventListenerConfig config;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, TrackerEvent>> trackerEventKafkaListenerContainerFactory() {
        Map<String, Object> props = getCommonConsumerProperties();

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
        props.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ErrorHandlingDeserializer2.VALUE_FUNCTION, FailedTrackerEventProvider.class);

        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TrackerEvent.class.getCanonicalName());

        ConsumerFactory<String, TrackerEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<String, TrackerEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(config.getConcurrency());
        factory.setConsumerFactory(consumerFactory);


        return factory;
    }

    public static class FailedTrackerEventProvider implements BiFunction<byte[], Headers, TrackerEvent> {
        private long failedCounter = 0L;

        @Override
        public TrackerEvent apply(byte[] t, Headers u) {
            if (log.isDebugEnabled()) {
                log.debug("=> Failed to deserialize TrackerEvent msg: '{}',\ttotal count: {}", new String(t), ++failedCounter);
            }
            return null;
        }
    }


    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, EventTrigger>> eventTriggerKafkaListenerContainerFactory() {
        Map<String, Object> props = getCommonConsumerProperties();

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
        props.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ErrorHandlingDeserializer2.VALUE_FUNCTION, FailedEventTriggerProvider.class);

        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EventTrigger.class.getCanonicalName());

        ConsumerFactory<String, EventTrigger> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<String, EventTrigger> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(config.getConcurrency());
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    public static class FailedEventTriggerProvider implements BiFunction<byte[], Headers, EventTrigger> {
        private long failedCounter = 0L;

        @Override
        public EventTrigger apply(byte[] t, Headers u) {
            if (log.isDebugEnabled()) {
                log.debug("=> Failed to deserialize EventTrigger msg: '{}',\ttotal count: {}", new String(t), ++failedCounter);
            }
            return null;
        }
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> stringKafkaListenerContainerFactory() {
        Map<String, Object> props = getCommonConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> consumerFactory
                = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    private Map<String, Object> getCommonConsumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

}
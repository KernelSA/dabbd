package ua.kernel.dabbd.tracking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
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
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.model.WaybillRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@EnableKafka
@Configuration
public class ReceiverConfig {

//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//    @Value("${spring.kafka.consumer.group-id}")
//    private String consumerGroupId;

    @Autowired
    private RouteTrackingProperties config;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, TrackerEvent>> trackerEventKafkaListenerContainerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getEventsConsumerGroup());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getEventsAutoOffsetResetConfig());

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
        props.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ErrorHandlingDeserializer2.VALUE_FUNCTION, FailedTrackerEventProvider.class);

        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TrackerEvent.class.getCanonicalName());

        ConsumerFactory<String, TrackerEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        ConcurrentKafkaListenerContainerFactory<String, TrackerEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(config.getEventsConcurrency());
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
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, WaybillRequest[]>> waybillKafkaListenerContainerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getWaybillConsumerGroup());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getWaybillAutoOffsetResetConfig());

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
        props.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ErrorHandlingDeserializer2.VALUE_FUNCTION, FailedWaybillRequestProvider.class);

        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WaybillRequest[].class.getCanonicalName());

        ConsumerFactory<String, WaybillRequest[]> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<String, WaybillRequest[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(config.getWaybillConcurrency());
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }


    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static class FailedWaybillRequestProvider implements BiFunction<byte[], Headers, WaybillRequest[]> {
        private long failedCounter = 0L;

        @Override
        public WaybillRequest[] apply(byte[] t, Headers u) {
            if (log.isTraceEnabled()) {
                log.trace("=> Failed to deserialize WaybillRequest msg: '{}',\ttotal count: {}", new String(t), ++failedCounter);
            }

            String data = new String(t);
            String data2 = data.substring(1, data.length() - 2);

            String strippedWaybill = data2.replaceAll("\\n", "");

            if (log.isTraceEnabled()) {
                log.trace("Will try to deserialize WaybillRequest: '{} ... {}'", strippedWaybill.substring(0, 300), strippedWaybill.substring(strippedWaybill.length() - 100));
            }
            WaybillRequest deserialize = null;
            try {
                if (strippedWaybill.length() < 2) {
                    return null;
                }
                if (mapper == null) {
                    mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                }
                deserialize = mapper.readValue(strippedWaybill.getBytes(), WaybillRequest.class);
            } catch (IOException e) {
                log.warn("Can't deserialize Waybill!", e);
            }

            return new WaybillRequest[]{deserialize};
        }
    }

    /*
        //FOR DEBUGGING
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
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getEventsConsumerGroup());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
*/

}
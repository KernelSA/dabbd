package ua.kernel.dabbd.commons.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class JacksonSerializationSchema<T> implements SerializationSchema<T> {

    private  transient ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final transient Class<T> type;


    public JacksonSerializationSchema(Class<T> type) {
        this.type = type;
    }

    @Override
    public byte[] serialize(T element) {
        try {
            if (mapper == null) {
                mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            }
            return mapper.writeValueAsBytes(element);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Cannot serialize object " + element, e);
        }

    }
}

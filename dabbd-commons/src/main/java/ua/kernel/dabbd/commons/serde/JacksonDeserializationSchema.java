package ua.kernel.dabbd.commons.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class JacksonDeserializationSchema<T> implements DeserializationSchema<T> {

    private transient ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final Class<T> type;


    public JacksonDeserializationSchema(Class<T> type) {
        this.type = type;
    }

    @Override
    public T deserialize(byte[] message) throws IOException {
        if (message == null || message.length < 2) {
            return null;
        }
        if (mapper == null) {
            mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        }
        return mapper.readValue(message, type);
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeInformation.of(type);
    }
}

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.serde.JacksonDeserializationSchema;

import java.util.Properties;
import java.util.UUID;

@Slf4j
public class WialonTopicTriggers {

    public static final String TOPIC = "json-topic";
    public static final StreamExecutionEnvironment ENV = StreamExecutionEnvironment.getExecutionEnvironment();

    public static void main(String[] args) throws Exception {
        log.info("> WialonTopicTriggers >>>>>>>");
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "ks-dmp-dev14.kyivstar.ua:6667,ks-dmp-dev15.kyivstar.ua:6667");
        properties.setProperty("group.id", "test-group-" + UUID.randomUUID().toString());
        properties.setProperty("auto.offset.reset", "earliest");
        System.out.println("kafka props: " + properties);
        DataStream<TrackerEvent> stream = ENV
                .addSource(new FlinkKafkaConsumer<>(TOPIC, new JacksonDeserializationSchema<>(TrackerEvent.class), properties));

////        System.out.println("add sink");
//       stream
//                .map(s -> "message: " + s.toString())
//                .print();

        stream.filter(value -> value.getFuelLevel() != null).countWindowAll(200).max("fuelLevel").print("max fuelLevel >>> ");

        stream.filter(value -> value.getPowerLevel() != null).countWindowAll(200).max("powerLevel").print("max powerLevel >>> ");

        stream.filter(value -> value.getSpeed() != null).countWindowAll(200).max("speed").print("max speed >>> ");

//        stream.keyBy(TrackerEvent::getTrackerId).countWindow(5).process();

        ENV.execute();

    }


}

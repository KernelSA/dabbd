import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.serde.JacksonSerializationSchema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MINUTES;

@Slf4j
public class FlinkTestApp {

    //    public static final String TOPIC = "CdrStream1";
    public static final String TOPIC = "json-topic";
    public static final StreamExecutionEnvironment ENV = StreamExecutionEnvironment.getExecutionEnvironment();

    public static void main(String[] args) throws Exception {
        log.info(">>>>>>>>");
//        ParameterTool parameterTool = ParameterTool.fromArgs(args);
//        Properties properties = new Properties();
//        properties.setProperty("bootstrap.servers", "ks-dmp23.kyivstar.ua:6667,ks-dmp24.kyivstar.ua:6667,ks-dmp25.kyivstar.ua:6667");
//        properties.setProperty("group.id", "test-group-1");
//        System.out.println("kafka props: " + properties);
//        DataStream<String> stream = ENV
//                .addSource(new FlinkKafkaConsumer<>(TOPIC, new SimpleStringSchema(), properties));
//
////        System.out.println("add sink");
//        stream
//                .map(s -> "message: " + Arrays.asList(s.split("\\|")))
//                .print();

        generateData();


    }


    public static void generateData() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "ks-dmp-dev14.kyivstar.ua:6667,ks-dmp-dev15.kyivstar.ua:6667");

        FlinkKafkaProducer<TrackerEvent> kafkaProducer = new FlinkKafkaProducer<>(TOPIC, new JacksonSerializationSchema<>(TrackerEvent.class), properties);
        kafkaProducer.setWriteTimestampToKafka(true);
        System.out.println("kafkaProducer " + kafkaProducer.toString());

        List<TrackerEvent> msgs = new ArrayList<>();
        try {
            for (int j = 0; j < 12; j++) {
                for (int i = 0; i < 1000; i++) {
//                msgs.add("test-message-" + i);
                    TrackerEvent e = new TrackerEvent();
                    LocalDateTime now = LocalDateTime.now().plus(i, MINUTES);
                    e.setEventDt(now);
                    e.setTrackerId("Tracker-" + j);
                    e.setFuelLevel(j * now.getNano());
                    e.setPowerLevel(j + now.getMinute());
                    e.setSpeed(j + now.getHour());
                    msgs.add(e);
                    System.out.println(". " + e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DataStreamSource<TrackerEvent> stringDataStreamSource = ENV.fromCollection(msgs);
        stringDataStreamSource.addSink(kafkaProducer);
        stringDataStreamSource.print();
        ENV.execute();

    }

}

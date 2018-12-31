import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import ua.kernel.dabbd.commons.model.EventTrigger;
import ua.kernel.dabbd.commons.model.TrackerEvent;
import ua.kernel.dabbd.commons.serde.JacksonDeserializationSchema;
import ua.kernel.dabbd.commons.serde.JacksonSerializationSchema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public class WialonTopicTriggersExample {

    public static final String TOPIC = "json-topic";
    public static final StreamExecutionEnvironment ENV = StreamExecutionEnvironment.getExecutionEnvironment();

    public static void main(String[] args) throws Exception {
        log.info("> WialonTopicTriggersExample >>>>>>>");
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "ks-dmp-dev14.kyivstar.ua:6667,ks-dmp-dev15.kyivstar.ua:6667");
        properties.setProperty("group.id", "test-group-" + UUID.randomUUID().toString());
        properties.setProperty("auto.offset.reset", "earliest");
        System.out.println("kafka props: " + properties);
        DataStream<TrackerEvent> stream = ENV
                .addSource(new FlinkKafkaConsumer<>(TOPIC, new JacksonDeserializationSchema<>(TrackerEvent.class), properties));

        FlinkKafkaProducer<EventTrigger> kafkaProducer = new FlinkKafkaProducer<>("triggers", new JacksonSerializationSchema<>(EventTrigger.class), properties);
        kafkaProducer.setWriteTimestampToKafka(true);


////        System.out.println("add sink");
//       stream
//                .map(s -> "message: " + s.toString())
//                .print();
//
//        stream.filter(value -> value.getFuelLevel() != null).countWindowAll(200).max("fuelLevel").print("max fuelLevel >>> ");
//
//        stream.filter(value -> value.getPowerLevel() != null).countWindowAll(200).max("powerLevel").print("max powerLevel >>> ");
//
//        stream.filter(value -> value.getSpeed() != null).countWindowAll(200).max("speed").print("max speed >>> ");


        SingleOutputStreamOperator<EventTrigger> process = stream
                .keyBy(TrackerEvent::getTrackerId)
//                .assignTimestampsAndWatermarks()
                .countWindow(2)
//                .trigger()
                .process(processWindowFunction());

        process.addSink(kafkaProducer);
        process.print("trigger >>>");

        ENV.execute();

    }

    private static ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow> processWindowFunction() {
        return new ProcessWindowFunction<TrackerEvent, EventTrigger, String, GlobalWindow>() {

            @Override
            public void process(String key, Context context, Iterable<TrackerEvent> elements, Collector<EventTrigger> out) {
                EventTrigger eventTrigger = new EventTrigger();
                eventTrigger.setTrackerId(key);
                eventTrigger.setTriggerDt(LocalDateTime.now());

                int count = 0;
                ArrayList<Integer> fuelLevels = new ArrayList<>();
                ArrayList<Integer> getPowerLevel = new ArrayList<>();
                ArrayList<Integer> getSpeed = new ArrayList<>();
                StringBuilder eventDates = new StringBuilder("eventDates: ");

                for (TrackerEvent event : elements) {
                    count++;
                    fuelLevels.add(event.getFuelLevel());
                    getPowerLevel.add(event.getPowerLevel());
                    getSpeed.add(event.getSpeed());
                    eventDates.append(event.getEventDt()).append(", ");

                }

                eventTrigger.setTriggerInfo("Count of elements: " + count + eventDates.toString()
                        + ", fuelLevels: " + fuelLevels.toString()
                        + ", getPowerLevel: " + getPowerLevel.toString()
                        + ", getSpeed: " + getSpeed.toString()
                );
                eventTrigger.setTriggerId("TEST-"+ UUID.randomUUID().toString());
                out.collect(eventTrigger);
            }
        };
    }
}

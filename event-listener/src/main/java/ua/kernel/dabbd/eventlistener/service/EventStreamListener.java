package ua.kernel.dabbd.eventlistener.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

//@Slf4j
//@Service
public class EventStreamListener {

//    @Autowired
    private KafkaConsumer<String,String> kafkaConsumer;

//    @PostConstruct
    public void test(){

        ConsumerRecords<String, String> poll = kafkaConsumer.poll(1000);
//        poll.

    }


}

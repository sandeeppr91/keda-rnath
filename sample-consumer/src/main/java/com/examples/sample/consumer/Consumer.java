package com.examples.sample.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Consumer {
    @KafkaListener(
            topics = "buy-icecream",
            containerFactory = "kafkaListenerContainerFactory")
    public void receive(
            ConsumerRecord<String, String> consumerRecord,
            Acknowledgment acknowledgment) {
        log.info(
                "received message='{}' from topic '{}'", consumerRecord.value(), consumerRecord.topic());
        log.info("Processing started for message {}",consumerRecord.value());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Shutting down due to error",e);
            System.exit(100);
        }
        log.info("Processing completed for message {}",consumerRecord.value());
        acknowledgment.acknowledge();
    }
}



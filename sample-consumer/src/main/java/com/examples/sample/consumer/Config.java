package com.examples.sample.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.Map;

@Configuration
public class Config {

  @Autowired private KafkaProperties kafkaProperties;

  public Map<String, Object> consumerConfig() {
    Map<String, Object> consumerConfig = kafkaProperties.buildConsumerProperties();
    consumerConfig.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 10000);
    consumerConfig.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    consumerConfig.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    consumerConfig.put(
            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
    consumerConfig.put(
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class);
    return consumerConfig;
  }
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
  kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    ConsumerFactory<String, String> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerConfig());
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

}

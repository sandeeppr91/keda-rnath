package com.examples.sample.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ProducerService {

  public static int timeout = 5000;
  private static long counter = 1L;

  private static boolean start = true;

  @Autowired private KafkaTemplate<String, String> producerTemplate;

  public <K, T> void send(String topic, K key, T payload, KafkaTemplate<K, T> template) {
    try {
      template
          .send(topic, key, payload)
          .addCallback(
              result ->
                  log.info(
                      "sent payload='{}' to topic='{}' to url '{}' with offset '{}'",
                      payload,
                      topic,
                      template
                          .getProducerFactory()
                          .getConfigurationProperties()
                          .get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG),
                      result != null ? result.getRecordMetadata().offset() : 0),
              ex -> log.error("unable to send message='{}'", payload, ex));
    } catch (KafkaException err) {
      throw err;
    }
  }

  @Async
  public void buyIcecreams() {
    if(!start){
      start = true;
    }
    while (start) {
      send("buy-icecream", UUID.randomUUID().toString(), "Ice Cream Order No -" + counter, producerTemplate);
      try {
        log.info("Waiting for next message");
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      counter++;
    }
  }

  public void summerSeason() {
    timeout = 1000;
  }

  public void winterSeason() {
    timeout = 5000;
  }

}

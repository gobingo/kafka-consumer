package com.mauryaa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaConsumerApplication {

  private static final int TIMEOUT_MILLIS = 5000;
  private static final int DEFAULT_RUNNING_AVERAGE = 0;
  private static final int AUTO_COMMIT_MILLIS = 3000;
  private static Configuration conf;
  private static Properties kafkaProps = new Properties();
  private static KafkaConsumer<String, String> consumer;

  @Autowired
  public void setConfiguration(Configuration conf) {
    KafkaConsumerApplication.conf = conf;
  }

	public static void main(String[] args) throws ExecutionException, InterruptedException {
    SpringApplication.run(KafkaConsumerApplication.class, args);

    String brokerList = conf.getBrokerList();
    String topic = conf.getTopic();

    ArrayList<Integer> buffer = new ArrayList<>();

    configure(brokerList);
    consumer.subscribe(Collections.singletonList(topic));

    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(TIMEOUT_MILLIS);

      records.iterator().forEachRemaining(record -> {
        System.out.printf("offset = %d, key = %s, value = %s\n",
          record.offset(), record.key(), record.value());

        try {
          buffer.add(Integer.valueOf(record.value()));
        } catch (NumberFormatException e) {
          // ignore
        }
      });

      System.out.println("running average for buffer " + buffer + " = " + getRunningAverage(buffer));
    }
  }

  private static double getRunningAverage(ArrayList<Integer> buffer) {
    return buffer.stream()
      .mapToDouble(value -> value)
      .average()
      .orElse(DEFAULT_RUNNING_AVERAGE);
  }

  private static void commitOffSet() {
    for (TopicPartition topicPartition : consumer.assignment()) {
      System.out.println("committing offset at position = " + consumer.position(topicPartition));
    }
    consumer.commitSync();
  }

  private static void configure(String servers) {
    kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
    kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    kafkaProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_MILLIS);
    kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
    kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
    kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
    consumer = new KafkaConsumer<String, String>(kafkaProps);
  }

}

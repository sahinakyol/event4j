package org.event4j.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@code KafkaConsumerService} class handles the consumption of messages
 * from a Kafka topic and republishes them using {@link KafkaProducerService}.
 */
public class KafkaConsumerService {

    private final KafkaConsumer<String, String> consumer;
    private final KafkaConfigurationProperties properties;
    private final KafkaProducerService kafkaProducerService;
    private final ScheduledExecutorService scheduler;

    /**
     * Constructs an instance of {@code KafkaConsumerService} with the specified
     * configuration properties.
     *
     * @param kafkaConfigurationProperties the Kafka configuration properties
     */
    public KafkaConsumerService(KafkaConfigurationProperties kafkaConfigurationProperties) {
        this.properties = kafkaConfigurationProperties;
        this.kafkaProducerService = new KafkaProducerService(kafkaConfigurationProperties);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getHosts());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getRetryGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Starts consuming messages from the configured Kafka topic and republishes
     * them using {@link KafkaProducerService}.
     */
    public void consumeMessages() {
        consumer.subscribe(Collections.singletonList(properties.getRetryTopic()));

        scheduler.scheduleAtFixedRate(() -> {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> consumedRecord : records) {
                    kafkaProducerService.publishMessage(consumedRecord.value());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}

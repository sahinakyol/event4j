package org.event4j.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * The {@code KafkaProducerService} class handles the publishing of messages
 * to Kafka topics and manages retries and error handling.
 */
public class KafkaProducerService {

    private final KafkaProducer<String, String> producer;
    private final KafkaConfigurationProperties properties;

    /**
     * Constructs an instance of {@code KafkaProducerService} with the specified
     * configuration properties.
     *
     * @param kafkaConfigurationProperties the Kafka configuration properties
     */
    public KafkaProducerService(KafkaConfigurationProperties kafkaConfigurationProperties) {
        this.properties = kafkaConfigurationProperties;
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getHosts());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }

    /**
     * Publishes a message to the configured Kafka topic. Retries on failures
     * up to the configured retry count.
     *
     * @param message the message to publish
     */
    public void publishMessage(String message) {
        int attempt = 0;
        while (attempt < properties.getRetryCount()) {
            try {
                producer.send(new ProducerRecord<>(properties.getTopic(), message)).get();
                return;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof RetriableException) {
                    attempt++;
                    if (attempt >= properties.getRetryCount()) {
                        retryMessage(message);
                        return;
                    }
                } else {
                    errorMessage(message, e.getMessage());
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                errorMessage(message, e.getMessage());
                return;
            } catch (Exception e) {
                errorMessage(message, e.getMessage());
                return;
            }
        }
    }

    /**
     * Retries publishing a message to the retry topic.
     *
     * @param message the message to retry
     */
    private void retryMessage(String message) {
        try {
            producer.send(new ProducerRecord<>(properties.getRetryTopic(), message)).get();
        } catch (InterruptedException | ExecutionException e) {
            errorMessage(message, e.getMessage());
        }
    }

    /**
     * Publishes an error message to the error topic with the original message
     * and the error details.
     *
     * @param message      the original message
     * @param errorMessage the error details
     */
    public void errorMessage(String message, String errorMessage) {
        try {
            List<Header> headers = List.of(
                    new RecordHeader("error-message", errorMessage.getBytes(StandardCharsets.UTF_8))
            );
            ProducerRecord<String, String> consumedRecord = new ProducerRecord<String, String>(properties.getErrorTopic(), null, null, message, headers);
            producer.send(consumedRecord).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}

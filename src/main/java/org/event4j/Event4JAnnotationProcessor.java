package org.event4j;

import org.event4j.kafka.KafkaConfigurationProperties;
import org.event4j.kafka.KafkaConsumerService;
import org.event4j.kafka.KafkaProducerService;
import org.event4j.kafka.KafkaPublisher;
import org.event4j.rest.ErrorLoggingService;
import org.event4j.rest.RestConfigurationProperties;
import org.event4j.rest.RestProducerService;
import org.event4j.rest.RestPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * The {@code Event4JAnnotationProcessor} class is responsible for processing
 * custom annotations related to Kafka and REST publishing. It initializes
 * the necessary services based on configuration properties and intercepts
 * annotated methods to handle message publishing.
 */
@Aspect
public class Event4JAnnotationProcessor {

    public final KafkaProducerService kafkaProducerService;
    private final RestProducerService restProducerService;
    private final ErrorLoggingService errorLoggingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructs an instance of {@code Event4JAnnotationProcessor}. Initializes
     * the Kafka and REST producer services based on the respective configuration
     * properties.
     */
    public Event4JAnnotationProcessor() {
        KafkaConfigurationProperties kafkaConfigurationProperties = new KafkaConfigurationProperties();
        RestConfigurationProperties restConfigurationProperties = new RestConfigurationProperties();
        if (kafkaConfigurationProperties.isEnable()) {
            this.kafkaProducerService = new KafkaProducerService(kafkaConfigurationProperties);
            KafkaConsumerService kafkaMessageConsumer = new KafkaConsumerService(kafkaConfigurationProperties);
            kafkaMessageConsumer.consumeMessages();
        } else {
            this.kafkaProducerService = null;
        }

        if (restConfigurationProperties.isEnable()) {
            this.restProducerService = new RestProducerService(restConfigurationProperties);
            this.errorLoggingService = new ErrorLoggingService(restConfigurationProperties);
        } else {
            this.restProducerService = null;
            this.errorLoggingService = null;
        }
    }

    /**
     * Pointcut that matches methods annotated with {@link KafkaPublisher}.
     *
     * @param kafkaPublisher the KafkaPublisher annotation
     */
    @Pointcut("@annotation(kafkaPublisher)")
    public void kafkaPublisherMethod(KafkaPublisher kafkaPublisher) {
    }

    /**
     * Around advice that processes methods annotated with {@link KafkaPublisher}.
     * It publishes the result of the method execution to a Kafka topic.
     *
     * @param joinPoint      the join point
     * @param kafkaPublisher the KafkaPublisher annotation
     * @throws Throwable if an error occurs during method execution
     */
    @Around("kafkaPublisherMethod(kafkaPublisher)")
    public void processKafkaPublisher(ProceedingJoinPoint joinPoint, KafkaPublisher kafkaPublisher) throws Throwable {
        if (kafkaProducerService == null) {
            return;
        }

        String message = "";
        try {
            message = objectMapper.writeValueAsString(joinPoint.proceed());
            kafkaProducerService.publishMessage(message);
        } catch (Exception e) {
            kafkaProducerService.errorMessage(message, e.getMessage());
        }
    }

    /**
     * Pointcut that matches methods annotated with {@link RestPublisher}.
     *
     * @param restPublisher the RestPublisher annotation
     */
    @Pointcut("@annotation(restPublisher)")
    public void restHandlerMethod(RestPublisher restPublisher) {
    }

    /**
     * Around advice that processes methods annotated with {@link RestPublisher}.
     * It sends the result of the method execution to a REST endpoint.
     *
     * @param joinPoint    the join point
     * @param restPublisher the RestPublisher annotation
     * @throws Throwable if an error occurs during method execution
     */
    @Around("restHandlerMethod(restPublisher)")
    public void processRestHandler(ProceedingJoinPoint joinPoint, RestPublisher restPublisher) throws Throwable {
        if (restProducerService == null) {
            return;
        }

        String message = "";
        try {
            message = objectMapper.writeValueAsString(joinPoint.proceed());
            restProducerService.send(message);
        } catch (Exception e) {
            errorLoggingService.logError(message, e.getMessage());
        }
    }
}

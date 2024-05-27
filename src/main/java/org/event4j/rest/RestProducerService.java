package org.event4j.rest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@code RestProducerService} class handles sending messages to a REST
 * endpoint and manages retries and error logging.
 */
public class RestProducerService {
    private final RestTemplate restTemplate;
    private final RestConfigurationProperties properties;
    private final ScheduledExecutorService scheduledExecutorService;

    private final ErrorLoggingService errorLoggingService;

    /**
     * Constructs an instance of {@code RestProducerService} with the specified
     * configuration properties.
     *
     * @param restConfigurationProperties the REST configuration properties
     */
    public RestProducerService(RestConfigurationProperties restConfigurationProperties) {
        this.properties = restConfigurationProperties;
        this.restTemplate = new RestTemplate();
        this.errorLoggingService = new ErrorLoggingService(restConfigurationProperties);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    /**
     * Sends a message to the configured REST endpoint.
     *
     * @param message the message to send
     * @return a {@code CompletableFuture} representing the asynchronous operation
     */
    public CompletableFuture<Void> send(String message) {
        return sendWithRetry(message, 0);
    }

    /**
     * Sends a message to the configured REST endpoint with retries.
     *
     * @param message the message to send
     * @param attempt the current retry attempt
     * @return a {@code CompletableFuture} representing the asynchronous operation
     */
    private CompletableFuture<Void> sendWithRetry(String message, int attempt) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> request = new HttpEntity<>(message, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(properties.getUrl(), request, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new InternalError(String.format("Request failed with status code : %d", response.getStatusCode().value()));
                }
            } catch (Exception e) {
                if (attempt + 1 < properties.getRetryCount()) {
                    scheduleRetry(message, attempt + 1);
                } else {
                    errorLoggingService.logError(message, e.getMessage());
                }
            }
        }, scheduledExecutorService);
    }

    /**
     * Schedules a retry for sending a message.
     *
     * @param message the message to send
     * @param attempt the current retry attempt
     */
    private void scheduleRetry(String message, int attempt) {
        scheduledExecutorService.schedule(() -> sendWithRetry(message, attempt), 2, TimeUnit.SECONDS);
    }
}

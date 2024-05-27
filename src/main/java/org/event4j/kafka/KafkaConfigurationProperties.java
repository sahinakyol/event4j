package org.event4j.kafka;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code KafkaConfigurationProperties} class loads and stores the
 * configuration properties for Kafka services.
 */
public class KafkaConfigurationProperties {
    private static final Logger LOGGER = Logger.getLogger(KafkaConfigurationProperties.class.getName());
    private String topic;
    private String retryTopic;
    private String errorTopic;
    private int retryCount;
    private String hosts;
    private String retryGroupId;
    private boolean enable;

    /**
     * Constructs an instance of {@code KafkaConfigurationProperties} and loads
     * the properties from the {@code application.properties} file.
     */
    public KafkaConfigurationProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                LOGGER.log(Level.WARNING, "Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);

            this.enable = Boolean.parseBoolean(properties.getProperty("event4j.kafka.enable"));
            this.topic = properties.getProperty("event4j.kafka.topic");
            this.retryTopic = properties.getProperty("event4j.kafka.retry-topic");
            this.errorTopic = properties.getProperty("event4j.kafka.error-topic");
            this.retryCount = Integer.parseInt(properties.getProperty("event4j.kafka.retry-count"));
            this.hosts = properties.getProperty("event4j.kafka.hosts");
            this.retryGroupId = properties.getProperty("event4j.kafka.retry-group-id");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Getters and setters for each property

    public String getRetryTopic() {
        return retryTopic;
    }

    public void setRetryTopic(String retryTopic) {
        this.retryTopic = retryTopic;
    }

    public String getErrorTopic() {
        return errorTopic;
    }

    public void setErrorTopic(String errorTopic) {
        this.errorTopic = errorTopic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getRetryGroupId() {
        return retryGroupId;
    }

    public void setRetryGroupId(String retryGroupId) {
        this.retryGroupId = retryGroupId;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}

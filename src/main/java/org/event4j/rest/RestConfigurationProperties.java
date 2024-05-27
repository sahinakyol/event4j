package org.event4j.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code RestConfigurationProperties} class loads and stores the
 * configuration properties for REST services.
 */
public class RestConfigurationProperties {
    private static final Logger LOGGER = Logger.getLogger(RestConfigurationProperties.class.getName());
    private String url;
    private String errorTable;
    private int retryCount;
    private String connectionUrl;
    private String connectionUser;
    private String connectionPassword;
    private boolean enable;

    /**
     * Constructs an instance of {@code RestConfigurationProperties} and loads
     * the properties from the {@code application.properties} file.
     */
    public RestConfigurationProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                LOGGER.log(Level.WARNING, "Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);

            this.enable = Boolean.parseBoolean(properties.getProperty("event4j.rest.enable"));
            this.url = properties.getProperty("event4j.rest.url");
            this.errorTable = properties.getProperty("event4j.rest.error-table");
            this.retryCount = Integer.parseInt(properties.getProperty("event4j.rest.retry-count"));
            this.connectionUrl = properties.getProperty("event4j.rest.connection-url");
            this.connectionUser = properties.getProperty("event4j.rest.connection-user");
            this.connectionPassword = properties.getProperty("event4j.rest.connection-password");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Getters and setters for each property

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getErrorTable() {
        return errorTable;
    }

    public void setErrorTable(String errorTable) {
        this.errorTable = errorTable;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionUser() {
        return connectionUser;
    }

    public void setConnectionUser(String connectionUser) {
        this.connectionUser = connectionUser;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}

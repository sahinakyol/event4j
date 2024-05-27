package org.event4j.rest;

import org.event4j.rest.exceptions.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The {@code ErrorLoggingService} class logs error messages to a database.
 */
public class ErrorLoggingService {

    private final Connection connection;
    private final RestConfigurationProperties properties;

    /**
     * Constructs an instance of {@code ErrorLoggingService} with the specified
     * configuration properties.
     *
     * @param restConfigurationProperties the REST configuration properties
     */
    public ErrorLoggingService(RestConfigurationProperties restConfigurationProperties) {
        this.properties = restConfigurationProperties;
        try {
            this.connection = DriverManager.getConnection(properties.getConnectionUrl(), properties.getConnectionUser(), properties.getConnectionPassword());
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to establish database connection", e);
        }
    }

    /**
     * Logs an error message to the database.
     *
     * @param message      the original message
     * @param errorMessage the error details
     */
    public void logError(String message, String errorMessage) {
        try {
            String sql = String.format("INSERT INTO %s (id, message, error_message, created_date) VALUES (?, ?, ?, ?)", properties.getErrorTable());
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, UUID.randomUUID().toString());
                pstmt.setString(2, message);
                pstmt.setString(3, errorMessage);
                pstmt.setString(4, LocalDateTime.now().toString());
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

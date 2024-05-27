package org.event4j.rest.exceptions;

/**
 * The {@code DatabaseConnectionException} is thrown when a database connection
 * cannot be established.
 */
public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

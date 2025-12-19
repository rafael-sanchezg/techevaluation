package com.pocs.techevaluation.techevaluation.domain.exception;

/**
 * Exception thrown when notification validation fails.
 */
public class NotificationValidationException extends RuntimeException {
    public NotificationValidationException(String message) {
        super(message);
    }
}


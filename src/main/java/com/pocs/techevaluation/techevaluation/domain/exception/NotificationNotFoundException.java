package com.pocs.techevaluation.techevaluation.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a notification is not found.
 */
public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID id) {
        super("Notification not found with id: " + id);
    }
}


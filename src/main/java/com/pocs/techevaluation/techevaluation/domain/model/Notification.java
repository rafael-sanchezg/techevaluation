package com.pocs.techevaluation.techevaluation.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Notification aggregate root.
 * Invariants: id, to, message, channel, priority, state, creationDate must be non-null/non-empty.
 */
public record Notification(
    UUID id,
    String to,
    String message,
    NotificationChannel channel,
    NotificationPriority priority,
    NotificationState state,
    BigDecimal cost,
    Instant creationTimestamp,
    Instant sendTimestamp
) {
    public Notification {
        Objects.requireNonNull(id, "id is required");
        if (to == null || to.isBlank()) throw new IllegalArgumentException("to is required");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("message is required");
        Objects.requireNonNull(channel, "channel is required");
        Objects.requireNonNull(priority, "priority is required");
        Objects.requireNonNull(state, "state is required");
        Objects.requireNonNull(cost, "cost is required");
        Objects.requireNonNull(creationTimestamp, "creationTimestamp is required");
        // sendTimestamp can be null if not sent yet
    }
}

/**
 * Notification channel types.
 */
enum NotificationChannel {
    EMAIL, SMS, PUSH
}

/**
 * Notification priority levels.
 */
enum NotificationPriority {
    HIGH, MEDIUM, LOW
}

/**
 * Notification state.
 */
enum NotificationState {
    PENDING, SENT, FAILED
}

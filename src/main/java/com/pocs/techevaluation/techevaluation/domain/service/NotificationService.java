package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.exception.NotificationNotFoundException;
import com.pocs.techevaluation.techevaluation.domain.exception.NotificationValidationException;
import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationChannel;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationPriority;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationState;
import com.pocs.techevaluation.techevaluation.domain.port.out.NotificationPersistencePort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for managing notification lifecycle.
 * Follows SOLID principles:
 * - SRP: Single responsibility - manages notification operations
 * - OCP: Open for extension (new channels via strategy), closed for modification
 * - DIP: Depends on abstractions (NotificationPersistencePort, NotificationChannelStrategyFactory)
 * - DRY: Reusable validation methods
 */
public class NotificationService {

    private static final int MAX_MESSAGE_LENGTH = 500;

    private final NotificationPersistencePort persistencePort;
    private final NotificationChannelStrategyFactory strategyFactory;

    public NotificationService(
            NotificationPersistencePort persistencePort,
            NotificationChannelStrategyFactory strategyFactory
    ) {
        this.persistencePort = persistencePort;
        this.strategyFactory = strategyFactory;
    }

    /**
     * Creates a new notification.
     *
     * @param to recipient identifier
     * @param message notification message
     * @param channel notification channel
     * @param priority notification priority
     * @return the created notification
     */
    public Notification createNotification(
            String to,
            String message,
            NotificationChannel channel,
            NotificationPriority priority
    ) {
        UUID id = generateId();
        NotificationChannelStrategy strategy = strategyFactory.getStrategy(channel);

        validateRecipient(to, strategy);
        validateMessage(message);

        // Create notification with PENDING state
        Notification notification = new Notification(
                id,
                to,
                message,
                channel,
                priority,
                NotificationState.PENDING,
                BigDecimal.ZERO,
                Instant.now(),
                null
        );

        // Calculate cost using strategy
        BigDecimal cost = strategy.calculateCost(notification);

        // Update notification with calculated cost
        notification = new Notification(
                notification.id(),
                notification.to(),
                notification.message(),
                notification.channel(),
                notification.priority(),
                notification.state(),
                cost,
                notification.creationTimestamp(),
                notification.sendTimestamp()
        );
        persistencePort.save(notification);
        return notification;
    }

    /**
     * Sends a notification using the appropriate channel strategy.
     *
     * @param notificationId the notification id to send
     * @return the sent notification with updated state and timestamp
     * @throws NotificationNotFoundException if notification not found
     */
    public Notification sendNotification(UUID notificationId) {
        Notification notification = persistencePort.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        // Get strategy for the notification's channel
        NotificationChannelStrategy strategy = strategyFactory.getStrategy(notification.channel());

        // Validate before sending
        strategy.validateRecipient(notification.to());

        // Send notification (strategy updates state and timestamp)
        Notification sentNotification = strategy.send(notification);

        // Save updated notification
        return persistencePort.save(sentNotification);
    }

    /**
     * Retrieves a notification by its id.
     *
     * @param id the notification id
     * @return the notification
     * @throws NotificationNotFoundException if notification not found
     */
    public Notification getById(UUID id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    /**
     * Retrieves all notifications with a specific state.
     *
     * @param state the notification state to filter by
     * @return list of notifications with the given state
     */
    public List<Notification> getByState(NotificationState state) {
        return persistencePort.filterByState(state);
    }

    /**
     * Calculates the total cost of all notifications.
     *
     * @return the total cost
     */
    public BigDecimal calculateTotalCost() {
        return persistencePort.listAll().stream()
                .map(Notification::cost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateRecipient(String to, NotificationChannelStrategy strategy) {
        strategy.validateRecipient(to);
    }

    /**
     * Validates the 'message' field.
     * DRY principle: reusable validation method.
     *
     * @param message notification message
     * @throws NotificationValidationException if validation fails
     */
    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new NotificationValidationException("Message cannot be null or empty");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new NotificationValidationException(
                    String.format("Message cannot exceed %d characters (current: %d)",
                            MAX_MESSAGE_LENGTH, message.length())
            );
        }
    }

    /**
     * Generates a unique identifier using UUID.
     * DRY principle: centralized ID generation.
     *
     * @return a new UUID
     */
    private UUID generateId() {
        return UUID.randomUUID();
    }
}


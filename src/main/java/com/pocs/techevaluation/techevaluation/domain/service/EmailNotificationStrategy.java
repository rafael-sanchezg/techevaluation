package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationChannel;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationState;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Email notification strategy implementation.
 * Cost: $0.10 per notification.
 * Validation: 'to' field must contain an @ symbol.
 */
public class EmailNotificationStrategy implements NotificationChannelStrategy {

    private static final BigDecimal EMAIL_COST = new BigDecimal("0.10");
    private static final String CHANNEL_NAME = "EMAIL";

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void validateRecipient(String to) {
        if (to == null || !to.contains("@")) {
            throw new IllegalArgumentException("Email address must contain an @ symbol");
        }
    }

    @Override
    public Notification send(Notification notification) {
        // Simulate sending email
        // In a real implementation, this would call an email service port
        System.out.println("Sending EMAIL via " + getChannelName() + " to: " + notification.to());

        return new Notification(
            notification.id(),
            notification.to(),
            notification.message(),
            notification.channel(),
            notification.priority(),
            NotificationState.SENT,
            calculateCost(notification),
            notification.creationTimestamp(),
            Instant.now()
        );
    }

    @Override
    public BigDecimal calculateCost(Notification notification) {
        return EMAIL_COST;
    }

    /**
     * Returns the channel name for this strategy.
     *
     * @return the channel name
     */
    public String getChannelName() {
        return CHANNEL_NAME;
    }
}


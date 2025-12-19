package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationChannel;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationState;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * SMS notification strategy implementation.
 * Cost: $0.50 per notification.
 * Validation: 'to' field must have exactly 10 numeric digits.
 */
public class SmsNotificationStrategy implements NotificationChannelStrategy {

    private static final BigDecimal SMS_COST = new BigDecimal("0.50");
    private static final String CHANNEL_NAME = "SMS";

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void validateRecipient(String to) {
        if (to == null || !to.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must have exactly 10 numeric digits");
        }
    }

    @Override
    public Notification send(Notification notification) {

        // Simulate sending SMS
        // In a real implementation, this would call an SMS service port
        System.out.println("Sending SMS via " + getChannelName() + " to: " + notification.to());

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
        return SMS_COST;
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


package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationChannel;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationState;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PUSH notification strategy implementation.
 * Cost: $0.05 per notification.
 * Validation: 'to' field must have the prefix "device_".
 */
public class PushNotificationStrategy implements NotificationChannelStrategy {

    private static final BigDecimal PUSH_COST = new BigDecimal("0.05");
    private static final String CHANNEL_NAME = "PUSH";
    private static final String DEVICE_PREFIX = "device_";

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void validate(Notification notification) {
        String to = notification.to();
        if (to == null || !to.startsWith(DEVICE_PREFIX)) {
            throw new IllegalArgumentException("Device ID must have the prefix 'device_'");
        }
    }

    @Override
    public Notification send(Notification notification) {
        validate(notification);

        // Simulate sending push notification
        // In a real implementation, this would call a push notification service port
        System.out.println("Sending PUSH via " + getChannelName() + " to: " + notification.to());

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
        return PUSH_COST;
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


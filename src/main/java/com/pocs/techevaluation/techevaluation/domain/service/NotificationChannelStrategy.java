package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationChannel;

/**
 * Strategy interface for sending notifications through different channels.
 * Each implementation handles a specific notification channel (EMAIL, SMS, PUSH).
 */
public interface NotificationChannelStrategy {


    NotificationChannel getChannel();

    void validateRecipient(String to);

    /**
     * Sends the notification through this channel.
     *
     * @param notification the notification to send
     * @return the send notification with updated state and timestamp
     */
    Notification send(Notification notification);

    /**
     * Calculates the cost of sending a notification through this channel.
     *
     * @param notification the notification to calculate cost for
     * @return the cost of sending the notification
     */
    java.math.BigDecimal calculateCost(Notification notification);
}


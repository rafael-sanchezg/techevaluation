package com.pocs.techevaluation.techevaluation.domain.port.out;

import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for notification persistence operations.
 * Following DIP: domain depends on abstraction, not concrete implementation.
 */
public interface NotificationPersistencePort {

    /**
     * Saves a notification.
     *
     * @param notification the notification to save
     * @return the saved notification
     */
    Notification save(Notification notification);

    /**
     * Finds a notification by its id.
     *
     * @param id the notification id
     * @return an Optional containing the notification if found
     */
    Optional<Notification> findById(UUID id);

    /**
     * Lists all notifications.
     *
     * @return list of all notifications
     */
    List<Notification> listAll();

    /**
     * Filters notifications by state.
     *
     * @param state the notification state to filter by
     * @return list of notifications with the given state
     */
    List<Notification> filterByState(NotificationState state);

    /**
     * Counts total number of notifications.
     *
     * @return the count of notifications
     */
    long countRegistries();

    /**
     * Clears all notifications from storage.
     */
    void cleanRepository();
}


package com.pocs.techevaluation.techevaluation.infrastructure.persistence.adapter;

import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationState;
import com.pocs.techevaluation.techevaluation.domain.port.out.NotificationPersistencePort;

import java.util.*;

/**
 * Fake in-memory repository implementation for testing and demonstration purposes.
 * Uses HashMap for fast lookups.
 * This is an adapter implementing the outbound persistence port.
 */
public class FakeNotificationRepository implements NotificationPersistencePort {

    private final Map<UUID, Notification> storage = new HashMap<>();

    @Override
    public Notification save(Notification notification) {
        storage.put(notification.id(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Notification> listAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Notification> filterByState(NotificationState state) {
        return storage.values().stream()
                .filter(notification -> notification.state() == state)
                .toList();
    }

    @Override
    public long countRegistries() {
        return storage.size();
    }

    @Override
    public void cleanRepository() {
        storage.clear();
    }
}


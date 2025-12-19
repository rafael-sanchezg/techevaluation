package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.model.NotificationChannel;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NotificationChannelStrategyFactory {

    private final Map<NotificationChannel, NotificationChannelStrategy> strategies;


    public NotificationChannelStrategyFactory(List<NotificationChannelStrategy> strategyList) {
        this.strategies = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelStrategy strategy : strategyList) {
            strategies.put(strategy.getChannel(), strategy);
        }
    }

    public NotificationChannelStrategy getStrategy(NotificationChannel channel) {
        NotificationChannelStrategy strategy = this.strategies.get(channel);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for channel: " + channel);
        }
        return strategy;
    }
}


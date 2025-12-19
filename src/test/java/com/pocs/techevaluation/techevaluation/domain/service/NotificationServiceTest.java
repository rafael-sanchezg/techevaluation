package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.model.Notification;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationChannel;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationPriority;
import com.pocs.techevaluation.techevaluation.domain.model.NotificationState;
import com.pocs.techevaluation.techevaluation.domain.port.out.NotificationPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationChannelStrategyFactory strategyFactory;

    @Mock
    private NotificationPersistencePort persistencePort;

    @InjectMocks
    private NotificationService notificationService;


    @Test
    void shouldCreateNotificationSuccessfully_when_data_is_valid() {
        // Arrage
        String to = "juan@gmail.com";;
        String message = "Test message";
        NotificationChannel channel = NotificationChannel.EMAIL;
        NotificationPriority priority = NotificationPriority.HIGH;
        BigDecimal expectedCost = BigDecimal.valueOf(0.10);

        NotificationChannelStrategy mockStrategy = mock(NotificationChannelStrategy.class);
        when(strategyFactory.getStrategy(channel)).thenReturn(mockStrategy);
        when(mockStrategy.calculateCost(any(Notification.class))).thenReturn(expectedCost);

        // Act
        Notification notification = notificationService.createNotification(to, message, channel, priority);

        // Assert
        assertNotNull(notification);
        assertEquals(NotificationState.PENDING, notification.state());
    }

    @Test
    void should_validate_to_field_for_email_channel_to_be_valid() {
        // Arrange
        String invalidTo = "juan-gmail.com"; // Missing '@'
        String message = "Test message";
        NotificationChannel channel = NotificationChannel.EMAIL;
        NotificationPriority priority = NotificationPriority.HIGH;

        NotificationChannelStrategy mockStrategy = mock(EmailNotificationStrategy.class);
        when(strategyFactory.getStrategy(channel)).thenReturn(mockStrategy);

        doThrow(new IllegalArgumentException("Email address must contain an @ symbol"))
                .when(mockStrategy).validateRecipient(invalidTo);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotification(invalidTo, message, channel, priority);
        });

        String expectedMessage = "Email address must contain an @ symbol";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
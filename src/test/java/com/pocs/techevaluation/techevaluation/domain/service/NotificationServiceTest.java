package com.pocs.techevaluation.techevaluation.domain.service;

import com.pocs.techevaluation.techevaluation.domain.exception.NotificationValidationException;
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

    @Test
    void should_validate_500_characters_limit_for_message_field() {

        String invalidTo = "1234567890";
        String message = "Test message with more than 500 characters Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(10); // > 500 char
        NotificationChannel channel = NotificationChannel.SMS;
        NotificationPriority priority = NotificationPriority.HIGH;

        NotificationChannelStrategy mockStrategy = mock(SmsNotificationStrategy.class);
        when(strategyFactory.getStrategy(channel)).thenReturn(mockStrategy);

        assertThrows(NotificationValidationException.class, () -> {
            notificationService.createNotification(invalidTo, message, channel, priority);
        });
    }

    @Test
    void should_send_notification_successfully_via_email_channel() {
        // Arrange
        String email = "juan@test.com"; // Valid email
        String message = "Test email notification";
        NotificationChannel channel = NotificationChannel.EMAIL;
        NotificationPriority priority = NotificationPriority.HIGH;
        BigDecimal expectedCost = BigDecimal.valueOf(0.10);

        NotificationChannelStrategy mockStrategy = mock(EmailNotificationStrategy.class);
        when(strategyFactory.getStrategy(channel)).thenReturn(mockStrategy);
        when(mockStrategy.calculateCost(any(Notification.class))).thenReturn(expectedCost);

        // Stub the save method to return a notification with ID
        when(persistencePort.save(any(Notification.class))).thenAnswer((Answer<Notification>) invocation -> {
            Notification arg = invocation.getArgument(0);
            return arg; // Return the notification as is
        });

        // Stub the findById method to return the created notification
        when(persistencePort.findById(any())).thenAnswer((Answer<java.util.Optional<Notification>>) invocation -> {
            return java.util.Optional.of(new Notification(
                    invocation.getArgument(0),
                    email,
                    message,
                    channel,
                    priority,
                    NotificationState.PENDING,
                    expectedCost,
                    java.time.Instant.now(),
                    null
            ));
        });

        // Stub the send method to return a SENT notification
        when(mockStrategy.send(any(Notification.class))).thenAnswer((Answer<Notification>) invocation -> {
            Notification arg = invocation.getArgument(0);
            return new Notification(
                    arg.id(),
                    arg.to(),
                    arg.message(),
                    arg.channel(),
                    arg.priority(),
                    NotificationState.SENT,
                    arg.cost(),
                    arg.creationTimestamp(),
                    java.time.Instant.now()
            );
        });

        // Act
        Notification createdNotification = notificationService.createNotification(email, message, channel, priority);
        Notification sentNotification = notificationService.sendNotification(createdNotification.id());

        // Assert
        assertNotNull(sentNotification);
        assertEquals(NotificationState.SENT, sentNotification.state());
        assertEquals(email, sentNotification.to());
        assertEquals(message, sentNotification.message());
        assertEquals(channel, sentNotification.channel());
        assertEquals(priority, sentNotification.priority());
        assertEquals(expectedCost, sentNotification.cost());
        assertNotNull(sentNotification.sendTimestamp());

        // Verify interactions
        verify(mockStrategy, times(2)).validateRecipient(email); // Once for create, once for send
        verify(mockStrategy).send(any(Notification.class));
        verify(persistencePort, times(2)).save(any(Notification.class)); // Once for create, once for send
    }

    @Test
    void should_calculate_total_cost_of_3_notifications_across_all_channels() {
        // Arrange
        // Define costs per channel
        BigDecimal emailCost = new BigDecimal("0.10"); // EMAIL cost
        BigDecimal smsCost = new BigDecimal("0.50");   // SMS cost
        BigDecimal pushCost = new BigDecimal("0.05");  // PUSH cost
        BigDecimal expectedTotalCost = emailCost.add(smsCost).add(pushCost); // 0.10 + 0.50 + 0.05 = 0.65

        // Create mock strategies for each channel
        NotificationChannelStrategy emailStrategy = mock(EmailNotificationStrategy.class);
        NotificationChannelStrategy smsStrategy = mock(SmsNotificationStrategy.class);
        NotificationChannelStrategy pushStrategy = mock(PushNotificationStrategy.class);

        // Configure strategy factory to return appropriate strategies
        when(strategyFactory.getStrategy(NotificationChannel.EMAIL)).thenReturn(emailStrategy);
        when(strategyFactory.getStrategy(NotificationChannel.SMS)).thenReturn(smsStrategy);
        when(strategyFactory.getStrategy(NotificationChannel.PUSH)).thenReturn(pushStrategy);

        // Configure cost calculation for each strategy
        when(emailStrategy.calculateCost(any(Notification.class))).thenReturn(emailCost);
        when(smsStrategy.calculateCost(any(Notification.class))).thenReturn(smsCost);
        when(pushStrategy.calculateCost(any(Notification.class))).thenReturn(pushCost);

        // Configure persistence port to save and return notifications
        when(persistencePort.save(any(Notification.class))).thenAnswer((Answer<Notification>) invocation ->
            invocation.getArgument(0)
        );

        // Create 3 notifications (one for each channel)
        Notification emailNotification = notificationService.createNotification(
            "user@example.com",
            "Email notification message",
            NotificationChannel.EMAIL,
            NotificationPriority.HIGH
        );

        Notification smsNotification = notificationService.createNotification(
            "1234567890",
            "SMS notification message",
            NotificationChannel.SMS,
            NotificationPriority.MEDIUM
        );

        Notification pushNotification = notificationService.createNotification(
            "device_abc123",
            "Push notification message",
            NotificationChannel.PUSH,
            NotificationPriority.LOW
        );

        // Configure persistence port to return all 3 notifications when listing all
        when(persistencePort.listAll()).thenReturn(
            java.util.List.of(emailNotification, smsNotification, pushNotification)
        );

        // Act
        BigDecimal totalCost = notificationService.calculateTotalCost();

        // Assert
        assertNotNull(totalCost);
        assertEquals(expectedTotalCost, totalCost);
        assertEquals(0, totalCost.compareTo(new BigDecimal("0.65"))); // Verify total is 0.65

        // Verify all notifications were created with correct costs
        assertEquals(emailCost, emailNotification.cost());
        assertEquals(smsCost, smsNotification.cost());
        assertEquals(pushCost, pushNotification.cost());

        // Verify strategy factory was called for each channel
        verify(strategyFactory).getStrategy(NotificationChannel.EMAIL);
        verify(strategyFactory).getStrategy(NotificationChannel.SMS);
        verify(strategyFactory).getStrategy(NotificationChannel.PUSH);

        // Verify persistence port saved all 3 notifications
        verify(persistencePort, times(3)).save(any(Notification.class));
        verify(persistencePort).listAll();
    }

    @Test
    void should_return_empty_list_when_no_notifications_exist_for_given_state() {
        // Arrange
        NotificationState state = NotificationState.SENT;

        // Configure persistence port to return empty list
        when(persistencePort.filterByState(state)).thenReturn(java.util.List.of());

        // Act
        var notifications = notificationService.getByState(state);

        // Assert
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
        assertEquals(0, notifications.size());

        // Verify persistence port was called with correct state
        verify(persistencePort).filterByState(state);
    }

    @Test
    void should_retrieve_correct_notifications_by_specific_state() {
        // Arrange
        NotificationState targetState = NotificationState.PENDING;

        // Create sample notifications with PENDING state
        Notification notification1 = new Notification(
            java.util.UUID.randomUUID(),
            "user1@example.com",
            "First pending notification",
            NotificationChannel.EMAIL,
            NotificationPriority.HIGH,
            NotificationState.PENDING,
            new BigDecimal("0.10"),
            java.time.Instant.now(),
            null
        );

        Notification notification2 = new Notification(
            java.util.UUID.randomUUID(),
            "1234567890",
            "Second pending notification",
            NotificationChannel.SMS,
            NotificationPriority.MEDIUM,
            NotificationState.PENDING,
            new BigDecimal("0.25"),
            java.time.Instant.now(),
            null
        );

        Notification notification3 = new Notification(
            java.util.UUID.randomUUID(),
            "device_xyz789",
            "Third pending notification",
            NotificationChannel.PUSH,
            NotificationPriority.LOW,
            NotificationState.PENDING,
            new BigDecimal("0.05"),
            java.time.Instant.now(),
            null
        );

        java.util.List<Notification> expectedNotifications = java.util.List.of(
            notification1, notification2, notification3
        );

        // Configure persistence port to return pending notifications
        when(persistencePort.filterByState(targetState)).thenReturn(expectedNotifications);

        // Act
        var actualNotifications = notificationService.getByState(targetState);

        // Assert
        assertNotNull(actualNotifications);
        assertFalse(actualNotifications.isEmpty());
        assertEquals(3, actualNotifications.size());

        // Verify all returned notifications have the correct state
        actualNotifications.forEach(notification ->
            assertEquals(NotificationState.PENDING, notification.state())
        );

        // Verify specific notifications are in the list
        assertTrue(actualNotifications.contains(notification1));
        assertTrue(actualNotifications.contains(notification2));
        assertTrue(actualNotifications.contains(notification3));

        // Verify persistence port was called with correct state
        verify(persistencePort).filterByState(targetState);
    }




}
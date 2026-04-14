package com.pulsenotify.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;
import com.pulsenotify.notification.dto.NotificationRequest;
import com.pulsenotify.notification.dto.NotificationResponse;
import com.pulsenotify.notification.event.NotificationEventPublisher;
import com.pulsenotify.notification.exception.NotificationNotFoundException;
import com.pulsenotify.notification.model.Notification;
import com.pulsenotify.notification.model.NotificationStatus;
import com.pulsenotify.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendNotification_savesEntityAndPublishesKafkaEvent() {
        // ARRANGE
        NotificationRequest request = new NotificationRequest(
            "user@example.com",
            "Test subject",
            NotificationChannel.EMAIL,
            "Hello!"
        );

        UUID generatedId = UUID.randomUUID();
        Notification savedNotification = Notification.builder()
            .id(generatedId)
            .recipient("user@example.com")
            .subject("Test subject")
            .channel(NotificationChannel.EMAIL)
            .messageBody("Hello!")
            .status(NotificationStatus.PENDING)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // ACT 
        NotificationResponse response = notificationService.sendNotification(request);

        // ASSERT
        assertThat(response.id()).isEqualTo(generatedId);
        assertThat(response.status()).isEqualTo(NotificationStatus.PENDING);
        verify(notificationEventPublisher).publishNotificationRequestedEvent(any(NotificationRequestedEvent.class));
    }

    @Test
    void getNotificationById_returnsResponseWhenFound() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        Notification notification = Notification.builder()
            .id(id)
            .recipient("user@example.com")
            .subject("Test subject")
            .channel(NotificationChannel.EMAIL)
            .messageBody("Hello!")
            .status(NotificationStatus.PENDING)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        // ACT
        NotificationResponse response = notificationService.getNotificationById(id);

        // ASSERT
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.recipient()).isEqualTo("user@example.com");
    }

    @Test
    void getNotificationById_throwsWhenNotFound() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> notificationService.getNotificationById(id))
            .isInstanceOf(NotificationNotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    @Test
    void getNotificationsByRecipient_returnsMappedList() {
        // ARRANGE
        String recipient = "user@example.com";
        Notification n1 = Notification.builder()
            .id(UUID.randomUUID())
            .recipient(recipient)
            .channel(NotificationChannel.EMAIL)
            .messageBody("Hello!")
            .status(NotificationStatus.PENDING)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Notification n2 = Notification.builder()
            .id(UUID.randomUUID())
            .recipient(recipient)
            .channel(NotificationChannel.EMAIL)
            .messageBody("Hello again!")
            .status(NotificationStatus.PENDING)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(notificationRepository.findByRecipient(recipient)).thenReturn(List.of(n1, n2));

        // ACT
        List<NotificationResponse> responses = notificationService.getNotificationsByRecipient(recipient);

        // ASSERT
        assertThat(responses).hasSize(2);
        assertThat(responses).allMatch(r -> r.recipient().equals(recipient));
    }


}

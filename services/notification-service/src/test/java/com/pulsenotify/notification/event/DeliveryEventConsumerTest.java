package com.pulsenotify.notification.event;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.notification.model.NotificationStatus;
import com.pulsenotify.notification.service.NotificationService;

@ExtendWith(MockitoExtension.class)
public class DeliveryEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DeliveryEventConsumer deliveryEventConsumer;

    @Test
    void handleDeliveryCompleted_marksNotificationSent() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
            .notificationId(id)
            .channel(NotificationChannel.EMAIL)
            .recipient("user@example.com")
            .providerMessageId("ses-123")
            .timestamp(Instant.now())
            .build();

        // ACT
        deliveryEventConsumer.handleDeliveryCompleted(event);

        // ASSERT
        verify(notificationService).markStatus(id, NotificationStatus.SENT);
    }

    @Test
    void handleDeliveryFailed_marksNotificationFailed() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        DeliveryFailedEvent event = DeliveryFailedEvent.builder()
            .notificationId(id)
            .channel(NotificationChannel.SMS)
            .recipient("+15550000000")
            .errorCode("DELIVERY_ERROR")
            .errorMessage("SNS error")
            .attemptNumber(1)
            .timestamp(Instant.now())
            .build();

        // ACT
        deliveryEventConsumer.handleDeliveryFailed(event);

        // ASSERT
        verify(notificationService).markStatus(id, NotificationStatus.FAILED);
    }
}

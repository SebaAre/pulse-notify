package com.pulsenotify.delivery.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pulsenotify.delivery.service.DeliveryService;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

@ExtendWith(MockitoExtension.class)
class DlqRetryConsumerTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private DlqRetryConsumer consumer;

    @Test
    void handleDlqMessage_whenUnderMax_retriesWithIncrementedAttempt() {
        NotificationRequestedEvent event = sampleEvent();
        byte[] attemptHeader = "1".getBytes(StandardCharsets.UTF_8);

        consumer.handleDlqMessage(event, attemptHeader, event.getNotificationId().toString());

        verify(deliveryService).processDelivery(eq(event), eq(2));
    }

    @Test
    void handleDlqMessage_whenAtMax_doesNotRetry() {
        NotificationRequestedEvent event = sampleEvent();
        byte[] attemptHeader = String.valueOf(DlqRetryConsumer.MAX_ATTEMPTS)
            .getBytes(StandardCharsets.UTF_8);

        consumer.handleDlqMessage(event, attemptHeader, event.getNotificationId().toString());

        verify(deliveryService, never()).processDelivery(any(), anyInt());
    }

    @Test
    void handleDlqMessage_whenHeaderMissing_defaultsAndRetries() {
        NotificationRequestedEvent event = sampleEvent();

        consumer.handleDlqMessage(event, null, event.getNotificationId().toString());

        verify(deliveryService).processDelivery(eq(event), eq(2));
    }

    private NotificationRequestedEvent sampleEvent() {
        return NotificationRequestedEvent.builder()
            .notificationId(UUID.randomUUID())
            .recipient("user@example.com")
            .channel(NotificationChannel.EMAIL)
            .subject("Test")
            .messageBody("Hello")
            .timestamp(Instant.now())
            .build();
    }
}

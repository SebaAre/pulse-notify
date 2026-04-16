package com.pulsenotify.delivery.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pulsenotify.delivery.event.DeliveryEventPublisher;
import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.DeliveryAttemptedEvent;
import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock
    private DeliveryEventPublisher deliveryEventPublisher;

    @Mock
    private DeliveryHandler handler;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(deliveryEventPublisher, List.of(handler));
    }

    @Test
    void processDelivery_successful() {
        //ARRANGE
        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
            .notificationId(UUID.randomUUID())
            .recipient("user@example.com")
            .channel(NotificationChannel.EMAIL)
            .subject("Test subject")
            .messageBody("Hello!")
            .timestamp(Instant.now())
            .build();
        
        when(handler.supports(NotificationChannel.EMAIL)).thenReturn(true);

        //ACT
        deliveryService.processDelivery(event, 1);
        
        //ASSERT
        verify(deliveryEventPublisher).publishAttempted(any(DeliveryAttemptedEvent.class));
        verify(deliveryEventPublisher).publishCompleted(any(DeliveryCompletedEvent.class));
        verify(deliveryEventPublisher, never()).publishFailed(any());

    }

    @Test
    void processDelivery_whenHandlerFails_publishesFailedEvent() {
        //ARRANGE
        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
            .notificationId(UUID.randomUUID())
            .recipient("user@example.com")
            .channel(NotificationChannel.EMAIL)
            .subject("Test subject")
            .messageBody("Hello!")
            .timestamp(Instant.now())
            .build();

        
        when(handler.supports(NotificationChannel.EMAIL)).thenReturn(true);
        doThrow(new DeliveryException("AWS error")).when(handler).send(any(NotificationRequestedEvent.class));

        //ACT
        deliveryService.processDelivery(event, 1);

        //ASSERT
        verify(deliveryEventPublisher).publishAttempted(any(DeliveryAttemptedEvent.class));
        verify(deliveryEventPublisher).publishFailed(any());
        verify(deliveryEventPublisher, never()).publishCompleted(any());
    }

    @Test
    void processDelivery_whenNoHandlerFound_publishesFailedEvent() {
        //ARRANGE
        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
            .notificationId(UUID.randomUUID())
            .recipient("user@example.com")
            .channel(NotificationChannel.SMS)
            .subject("Test subject")
            .messageBody("Hello!")
            .timestamp(Instant.now())
            .build();

        when(handler.supports(NotificationChannel.SMS)).thenReturn(false);

        //ACT
        deliveryService.processDelivery(event, 1);

        //ASSERT
        verify(deliveryEventPublisher).publishAttempted(any(DeliveryAttemptedEvent.class));
        verify(deliveryEventPublisher).publishFailed(any());
        verify(deliveryEventPublisher, never()).publishCompleted(any());
    }

}
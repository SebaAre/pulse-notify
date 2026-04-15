package com.pulsenotify.delivery.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pulsenotify.delivery.event.DeliveryEventPublisher;
import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.DeliveryAttemptedEvent;
import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryEventPublisher deliveryEventPublisher;

    private final List<DeliveryHandler> handlers;

    public void processDelivery(NotificationRequestedEvent event) {

        deliveryEventPublisher.publishAttempted(
            DeliveryAttemptedEvent.builder()
                .notificationId(event.getNotificationId())
                .channel(event.getChannel())
                .recipient(event.getRecipient())
                .attemptNumber(1)
                .timestamp(Instant.now())
                .build()
        );

        try {
            handlers.stream()
                .filter(h -> h.supports(event.getChannel()))
                .findFirst()
                .orElseThrow(() -> new DeliveryException("No handler found for channel: " + event.getChannel()))
                .send(event);

            deliveryEventPublisher.publishCompleted(
                DeliveryCompletedEvent.builder()
                    .notificationId(event.getNotificationId())
                    .channel(event.getChannel())
                    .recipient(event.getRecipient())
                    .timestamp(Instant.now())
                    .build()
            );

        } catch (DeliveryException deliveryException) {
            deliveryEventPublisher.publishFailed(
                DeliveryFailedEvent.builder()
                    .notificationId(event.getNotificationId())
                    .channel(event.getChannel())
                    .recipient(event.getRecipient())
                    .errorCode("DELIVERY_ERROR")
                    .errorMessage(deliveryException.getMessage())
                    .attemptNumber(1)
                    .timestamp(Instant.now())
                    .build()
            );
        }
    }
}
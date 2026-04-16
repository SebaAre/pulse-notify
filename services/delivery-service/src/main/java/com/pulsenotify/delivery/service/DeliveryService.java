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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryEventPublisher deliveryEventPublisher;

    private final List<DeliveryHandler> handlers;

    public void processDelivery(NotificationRequestedEvent event, int attemptNumber) {

        deliveryEventPublisher.publishAttempted(
            DeliveryAttemptedEvent.builder()
                .notificationId(event.getNotificationId())
                .channel(event.getChannel())
                .recipient(event.getRecipient())
                .attemptNumber(attemptNumber)
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
                    .attemptNumber(attemptNumber)
                    .timestamp(Instant.now())
                    .build()
            );
             log.warn("Delivery failed permanently for notificationId={}, channel={}, attempt={}. Message sent to DLQ for manual review.",
                event.getNotificationId(), event.getChannel(), attemptNumber);
        }
    }
}
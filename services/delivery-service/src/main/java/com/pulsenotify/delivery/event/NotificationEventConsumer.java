package com.pulsenotify.delivery.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {
    
    private final DeliveryService deliveryService;

    @KafkaListener(topics = "notification.requested", groupId = "delivery-service")
    public void handleNotificationRequested(NotificationRequestedEvent event) {
        deliveryService.processDelivery(event);
    }

}
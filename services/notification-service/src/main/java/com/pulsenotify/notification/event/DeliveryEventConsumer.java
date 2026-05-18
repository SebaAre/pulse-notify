package com.pulsenotify.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;
import com.pulsenotify.notification.model.NotificationStatus;
import com.pulsenotify.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "delivery.completed", groupId = "notification-service")
    public void handleDeliveryCompleted(DeliveryCompletedEvent event) {
        notificationService.markStatus(event.getNotificationId(), NotificationStatus.SENT);
    }

    @KafkaListener(topics = "delivery.failed", groupId = "notification-service")
    public void handleDeliveryFailed(DeliveryFailedEvent event) {
        notificationService.markStatus(event.getNotificationId(), NotificationStatus.FAILED);
    }
}

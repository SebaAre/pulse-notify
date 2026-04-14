package com.pulsenotify.notification.event;

import com.pulsenotify.events.NotificationRequestedEvent;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
    
    private final KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate;

    public void publishNotificationRequestedEvent (NotificationRequestedEvent event) {
        kafkaTemplate.send("notification.requested", event.getNotificationId().toString(), event);
    }

}
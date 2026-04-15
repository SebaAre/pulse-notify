package com.pulsenotify.delivery.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.pulsenotify.events.DeliveryAttemptedEvent;
import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAttempted(DeliveryAttemptedEvent event){
        kafkaTemplate.send("delivery.attempted", event.getNotificationId().toString(), event);
    }

    public void publishCompleted(DeliveryCompletedEvent event){
        kafkaTemplate.send("delivery.completed", event.getNotificationId().toString(), event);
    }

    public void publishFailed(DeliveryFailedEvent event){
        kafkaTemplate.send("delivery.failed", event.getNotificationId().toString(), event);
    }


}
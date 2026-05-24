package com.pulsenotify.delivery.event;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.pulsenotify.events.DeliveryAttemptedEvent;
import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryEventPublisher {

    public static final String DLQ_TOPIC = "delivery.failed.dlq";
    public static final String ATTEMPT_HEADER = "x-delivery-attempt";

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

    public void publishToDlq(NotificationRequestedEvent event, int attemptNumber) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
            DLQ_TOPIC,
            event.getNotificationId().toString(),
            event
        );
        record.headers().add(
            ATTEMPT_HEADER,
            Integer.toString(attemptNumber).getBytes(StandardCharsets.UTF_8)
        );
        kafkaTemplate.send(record);
    }

}
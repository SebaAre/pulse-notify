package com.pulsenotify.delivery.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.pulsenotify.delivery.service.DeliveryService;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqRetryConsumer {

    static final int MAX_ATTEMPTS = 3;

    private final DeliveryService deliveryService;

    @KafkaListener(topics = DeliveryEventPublisher.DLQ_TOPIC, groupId = "delivery-service-dlq")
    public void handleDlqMessage(
        @Payload NotificationRequestedEvent event,
        @Header(name = DeliveryEventPublisher.ATTEMPT_HEADER, required = false) byte[] attemptHeader,
        @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        int previousAttempt = parseAttempt(attemptHeader);

        if (previousAttempt >= MAX_ATTEMPTS) {
            log.error("DLQ retry exhausted for notificationId={} key={} after {} attempts; dropping message.",
                event.getNotificationId(), key, previousAttempt);
            return;
        }

        int nextAttempt = previousAttempt + 1;
        log.info("Retrying delivery from DLQ for notificationId={} attempt={}",
            event.getNotificationId(), nextAttempt);
        deliveryService.processDelivery(event, nextAttempt);
    }

    private int parseAttempt(byte[] header) {
        if (header == null) {
            return 1;
        }
        try {
            return Integer.parseInt(new String(header));
        } catch (NumberFormatException ex) {
            log.warn("Invalid {} header value; defaulting to 1", DeliveryEventPublisher.ATTEMPT_HEADER);
            return 1;
        }
    }
}

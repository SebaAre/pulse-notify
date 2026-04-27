package com.pulsenotify.audit.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pulsenotify.audit.repository.AuditEventRepository;
import com.pulsenotify.audit.service.AuditEventMapper;
import com.pulsenotify.events.DeliveryAttemptedEvent;
import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;
import com.pulsenotify.events.NotificationProcessedEvent;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditEventRepository auditEventRepository;

    @KafkaListener(topics = "notification.requested")
    public void onNotificationRequestedEvent(NotificationRequestedEvent event) {
        auditEventRepository.save(AuditEventMapper.from(event));
        log.info("audited NOTIFICATION_REQUESTED notificationId={}", event.getNotificationId());

    }

    @KafkaListener(topics = "notification.processed")
    public void onNotificationProcessedEvent(NotificationProcessedEvent event) {
        auditEventRepository.save(AuditEventMapper.from(event));
        log.info("audited NOTIFICATION_PROCESSED notificationId={}", event.getNotificationId());
    }
    
    @KafkaListener(topics = "delivery.attempted")
    public void onDeliveryAttemptedEvent(DeliveryAttemptedEvent event) {
        auditEventRepository.save(AuditEventMapper.from(event));
        log.info("audited DELIVERY_ATTEMPTED notificationId={}", event.getNotificationId());
    }

    @KafkaListener(topics = "delivery.completed")
    public void onDeliveryCompletedEvent(DeliveryCompletedEvent event) {
        auditEventRepository.save(AuditEventMapper.from(event));
        log.info("audited DELIVERY_COMPLETED notificationId={}", event.getNotificationId());
    }

    @KafkaListener(topics = "delivery.failed")
    public void onDeliveryFailedEvent(DeliveryFailedEvent event) {
        auditEventRepository.save(AuditEventMapper.from(event));
        log.info("audited DELIVERY_FAILED notificationId={}", event.getNotificationId());
    }

}
package com.pulsenotify.audit.service;

import com.pulsenotify.audit.model.AuditEvent;
import com.pulsenotify.events.DeliveryAttemptedEvent;
import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;
import com.pulsenotify.events.NotificationProcessedEvent;
import com.pulsenotify.events.NotificationRequestedEvent;

public final class AuditEventMapper {

    private AuditEventMapper() {}

    public static AuditEvent from(NotificationRequestedEvent event) {
        return AuditEvent.builder()
                .notificationId(event.getNotificationId().toString())
                .timestamp(event.getTimestamp().toString())
                .eventType("NOTIFICATION_REQUESTED")
                .channel(event.getChannel() != null ? event.getChannel().name() : null)
                .recipient(event.getRecipient())
                .build();
    }

    public static AuditEvent from(NotificationProcessedEvent event) {
        return AuditEvent.builder()
                .notificationId(event.getNotificationId().toString())
                .timestamp(event.getTimestamp().toString())
                .eventType("NOTIFICATION_PROCESSED_" + (event.getStatus() != null ? event.getStatus().name() : "UNKNOWN"))
                .build();
    }

    public static AuditEvent from(DeliveryAttemptedEvent event) {
        return AuditEvent.builder()
                .notificationId(event.getNotificationId().toString())
                .timestamp(event.getTimestamp().toString())
                .eventType("DELIVERY_ATTEMPTED")
                .channel(event.getChannel() != null ? event.getChannel().name() : null)
                .recipient(event.getRecipient())
                .attemptNumber(event.getAttemptNumber())
                .build();
    }

    public static AuditEvent from(DeliveryCompletedEvent event) {
        return AuditEvent.builder()
                .notificationId(event.getNotificationId().toString())
                .timestamp(event.getTimestamp().toString())
                .eventType("DELIVERY_COMPLETED")
                .channel(event.getChannel() != null ? event.getChannel().name() : null)
                .recipient(event.getRecipient())
                .providerMessageId(event.getProviderMessageId())
                .build();
    }

    public static AuditEvent from(DeliveryFailedEvent event) {
        return AuditEvent.builder()
                .notificationId(event.getNotificationId().toString())
                .timestamp(event.getTimestamp().toString())
                .eventType("DELIVERY_FAILED")
                .channel(event.getChannel() != null ? event.getChannel().name() : null)
                .recipient(event.getRecipient())
                .attemptNumber(event.getAttemptNumber())
                .errorCode(event.getErrorCode())
                .errorMessage(event.getErrorMessage())
                .build();
    }
}
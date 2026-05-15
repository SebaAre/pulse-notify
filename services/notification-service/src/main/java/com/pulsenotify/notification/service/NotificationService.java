package com.pulsenotify.notification.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;
import com.pulsenotify.notification.dto.NotificationRequest;
import com.pulsenotify.notification.dto.NotificationResponse;
import com.pulsenotify.notification.dto.NotificationStatsResponse;
import com.pulsenotify.notification.event.NotificationEventPublisher;
import com.pulsenotify.notification.exception.NotificationNotFoundException;
import com.pulsenotify.notification.model.Notification;
import com.pulsenotify.notification.model.NotificationStatus;
import com.pulsenotify.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventPublisher eventPublisher;

    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {

        Notification notification = Notification.builder()
            .recipient(request.recipient())
            .subject(request.subject())
            .channel(request.channel())
            .messageBody(request.messageBody())
            .status(NotificationStatus.PENDING)
            .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
            .notificationId(savedNotification.getId())
            .recipient(savedNotification.getRecipient())
            .channel(savedNotification.getChannel())
            .subject(savedNotification.getSubject())
            .messageBody(savedNotification.getMessageBody())
            .timestamp(savedNotification.getCreatedAt())
            .build();

        eventPublisher.publishNotificationRequestedEvent(event);

        return toResponse(savedNotification);

    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new NotificationNotFoundException(id));

        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByRecipient(String recipient) {

        return notificationRepository.findByRecipient(recipient)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public NotificationStatsResponse getStats() {

        Map<NotificationStatus, Long> byStatus = new EnumMap<>(NotificationStatus.class);
        for (NotificationStatus s : NotificationStatus.values()) {
            byStatus.put(s, 0L);
        }
        notificationRepository.countGroupedByStatus()
            .forEach(row -> byStatus.put(row.getStatus(), row.getCount()));

        Map<NotificationChannel, Long> byChannel = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannel c : NotificationChannel.values()) {
            byChannel.put(c, 0L);
        }
        notificationRepository.countGroupedByChannel()
            .forEach(row -> byChannel.put(row.getChannel(), row.getCount()));

        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();

        return new NotificationStatsResponse(total, byStatus, byChannel);
    }

    private NotificationResponse toResponse(Notification notification) {

        return new NotificationResponse(
            notification.getId(),
            notification.getRecipient(),
            notification.getSubject(),
            notification.getChannel(),
            notification.getMessageBody(),
            notification.getStatus(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
        );
    }

}

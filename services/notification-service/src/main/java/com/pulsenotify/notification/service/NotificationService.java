package com.pulsenotify.notification.service;

import java.util.List;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsenotify.events.NotificationRequestedEvent;
import com.pulsenotify.notification.dto.NotificationRequest;
import com.pulsenotify.notification.dto.NotificationResponse;
import com.pulsenotify.notification.exception.NotificationNotFoundException;
import com.pulsenotify.notification.model.Notification;
import com.pulsenotify.notification.model.NotificationStatus;
import com.pulsenotify.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, NotificationRequestedEvent> kafkaTemplate;
    
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

        kafkaTemplate.send("notification.requested", savedNotification.getId().toString(), event);

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

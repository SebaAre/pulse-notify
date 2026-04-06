package com.pulsenotify.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.notification.model.NotificationStatus;

public record NotificationResponse(

    UUID id,

    String recipient,

    String subject,

    NotificationChannel channel,

    String messageBody,

    NotificationStatus status,

    Instant createdAt,

    Instant updatedAt


) {}
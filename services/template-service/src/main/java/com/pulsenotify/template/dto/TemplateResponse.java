package com.pulsenotify.template.dto;

import com.pulsenotify.events.NotificationChannel;
import java.time.Instant;
import java.util.UUID;

public record TemplateResponse(

    UUID id,

    String name,

    NotificationChannel channel,

    String subject,

    String body,

    Instant createdAt,

    Instant updatedAt
    
) {}
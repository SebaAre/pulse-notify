package com.pulsenotify.events;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationProcessedEvent {
    
    private UUID notificationId;
    private NotificationEventStatus status;
    private Instant timestamp;

}

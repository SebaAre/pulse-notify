package com.pulsenotify.events;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryCompletedEvent {
    
    private UUID notificationId;
    private NotificationChannel channel;
    private String recipient;
    private String providerMessageId;
    private Instant timestamp;

}

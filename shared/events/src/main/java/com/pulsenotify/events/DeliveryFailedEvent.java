package com.pulsenotify.events;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFailedEvent {
    
    private UUID notificationId;
    private NotificationChannel channel;
    private String recipient;
    private String errorCode;
    private String errorMessage;
    private int attemptNumber;
    private Instant timestamp;

}

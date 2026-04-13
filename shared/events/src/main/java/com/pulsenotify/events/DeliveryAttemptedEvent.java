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
public class DeliveryAttemptedEvent {

    private UUID notificationId;
    private NotificationChannel channel;
    private String recipient;
    private int attemptNumber;
    private Instant timestamp;

}
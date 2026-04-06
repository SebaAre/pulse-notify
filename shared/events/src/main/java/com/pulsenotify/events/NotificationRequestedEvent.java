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
public class NotificationRequestedEvent {
    

    private UUID notificationId;
    private String recipient;
    private NotificationChannel channel;
    private String subject;
    private String messageBody;
    private Instant timestamp;


}
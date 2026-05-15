package com.pulsenotify.notification.dto;

import java.util.Map;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.notification.model.NotificationStatus;

public record NotificationStatsResponse(

    long total,

    Map<NotificationStatus, Long> byStatus,

    Map<NotificationChannel, Long> byChannel

) {}

package com.pulsenotify.notification.repository;

import com.pulsenotify.events.NotificationChannel;

public interface ChannelCount {
    NotificationChannel getChannel();
    long getCount();
}

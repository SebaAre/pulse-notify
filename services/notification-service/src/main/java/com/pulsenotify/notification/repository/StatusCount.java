package com.pulsenotify.notification.repository;

import com.pulsenotify.notification.model.NotificationStatus;

public interface StatusCount {
    NotificationStatus getStatus();
    long getCount();
}

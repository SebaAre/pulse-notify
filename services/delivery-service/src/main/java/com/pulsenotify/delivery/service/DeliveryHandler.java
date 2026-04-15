package com.pulsenotify.delivery.service;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

public interface DeliveryHandler {
    
    boolean supports(NotificationChannel channel);

    void send(NotificationRequestedEvent event);

}
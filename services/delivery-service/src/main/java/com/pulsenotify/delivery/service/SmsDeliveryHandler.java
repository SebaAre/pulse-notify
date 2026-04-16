package com.pulsenotify.delivery.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sns.SnsClient;

@Component
@RequiredArgsConstructor
public class SmsDeliveryHandler implements DeliveryHandler {

    private final SnsClient snsClient;

    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.SMS == channel;
    }

    @Retryable(
        retryFor = DeliveryException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public void send(NotificationRequestedEvent event) {
        try {
            snsClient.publish(builder -> builder
                .message(event.getMessageBody())
                .phoneNumber(event.getRecipient())
            );
        } catch (Exception e) {
            throw new DeliveryException("Failed to send SMS: " + e.getMessage(), e);
        }
    }
    
}
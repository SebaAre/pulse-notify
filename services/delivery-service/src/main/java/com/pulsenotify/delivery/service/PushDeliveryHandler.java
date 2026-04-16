package com.pulsenotify.delivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@RequiredArgsConstructor
public class PushDeliveryHandler implements DeliveryHandler {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.webhook-queue-url}")
    private String pushQueueUrl;
    
    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.PUSH == channel;
    }

    @Retryable(
        retryFor = DeliveryException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override  
    public void send(NotificationRequestedEvent event) {
        try {
            sqsClient.sendMessage(builder -> builder
                .queueUrl(pushQueueUrl)
                .messageBody(event.getMessageBody())
            );
        } catch (Exception e) {
            throw new DeliveryException("Failed to send push notification: " + e.getMessage(), e);
        }
    }

}
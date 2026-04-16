package com.pulsenotify.delivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Component
@RequiredArgsConstructor
public class EmailDeliveryHandler implements DeliveryHandler {
    
    private final SesClient sesClient;

    @Value("${aws.ses.from-address}")
    private String fromAddress;

    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.EMAIL == channel;
    }

    @Retryable(
        retryFor = DeliveryException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public void send(NotificationRequestedEvent event) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
            .source(fromAddress)
            .destination(
                Destination.builder()
                    .toAddresses(event.getRecipient())
                    .build()
            )
            .message(
                Message.builder()
                    .subject(
                        Content.builder()
                            .data(event.getSubject())
                            .build()
                    )
                    .body(
                        Body.builder()
                            .text(
                                Content.builder()
                                    .data(event.getMessageBody())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();

            sesClient.sendEmail(request); 

        } catch (Exception e) {
            throw new DeliveryException("Failed to send email: " + e.getMessage(), e);
        }
    }

}

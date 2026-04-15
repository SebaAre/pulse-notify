package com.pulsenotify.delivery.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pulsenotify.delivery.event.DeliveryEventPublisher;
import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.DeliveryAttemptedEvent;
import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.DeliveryFailedEvent;
import com.pulsenotify.events.NotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryEventPublisher deliveryEventPublisher;

    private final SesClient sesClient;

    private final SnsClient snsClient;

    private final SqsClient sqsClient;

    @Value("${aws.ses.from-address}")
    private String fromAddress;

    @Value("${aws.sqs.webhook-queue-url}")
    private String webhookQueueUrl;

    public void processDelivery(NotificationRequestedEvent event) {

        deliveryEventPublisher.publishAttempted(
            DeliveryAttemptedEvent.builder()
                .notificationId(event.getNotificationId())
                .channel(event.getChannel())
                .recipient(event.getRecipient())
                .attemptNumber(1)
                .timestamp(Instant.now())
                .build()
        );

        try {
            switch (event.getChannel()) {
                case EMAIL -> sendEmail(event);
                case SMS   -> sendSms(event);
                case PUSH  -> sendPush(event);
            }

            deliveryEventPublisher.publishCompleted(
                DeliveryCompletedEvent.builder()
                    .notificationId(event.getNotificationId())
                    .channel(event.getChannel())
                    .recipient(event.getRecipient())
                    .timestamp(Instant.now())
                    .build()
            );

        } catch (DeliveryException deliveryException) {
            deliveryEventPublisher.publishFailed(
                DeliveryFailedEvent.builder()
                    .notificationId(event.getNotificationId())
                    .channel(event.getChannel())
                    .recipient(event.getRecipient())
                    .errorCode("DELIVERY_ERROR")
                    .errorMessage(deliveryException.getMessage())
                    .attemptNumber(1)
                    .timestamp(Instant.now())
                    .build()
            );
        }
    }

    private void sendEmail(NotificationRequestedEvent event) {
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

    private void sendSms(NotificationRequestedEvent event) {
        try {
            snsClient.publish(p -> p
                .phoneNumber(event.getRecipient())
                .message(event.getMessageBody())
            );
        } catch (Exception e) {
            throw new DeliveryException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    private void sendPush(NotificationRequestedEvent event) {
        try {
             sqsClient.sendMessage(p -> p
                .queueUrl(webhookQueueUrl)
                .messageBody(event.getMessageBody())
            );
        } catch (Exception e) {
            throw new DeliveryException("Failed to send Push Notification: " + e.getMessage(), e);
        }
    }
}
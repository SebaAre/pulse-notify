package com.pulsenotify.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@ExtendWith(MockitoExtension.class)
public class PushDeliveryHandlerTest {
    
    @Mock
    private SqsClient sqsClient;

    @InjectMocks
    private PushDeliveryHandler pushDeliveryHandler;

    @Test
    void supports_returnTrue_forPushChannel() {
        assertThat(pushDeliveryHandler.supports(NotificationChannel.PUSH)).isTrue();
    }

    @Test
    void supports_returnFalse_forNonPushChannel() {
        assertThat(pushDeliveryHandler.supports(NotificationChannel.EMAIL)).isFalse();
    }

    @Test
    void send_throwsDeliveryException_whenSqsClientFails() {
        // ARRANGE
        ReflectionTestUtils.setField(pushDeliveryHandler, "pushQueueUrl", "https://sqs.test/queue");

        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
            .notificationId(UUID.randomUUID())
            .recipient("user@example.com")
            .channel(NotificationChannel.PUSH)
            .subject("Test subject")
            .messageBody("Hello!")
            .timestamp(Instant.now())
            .build();

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))    
        .thenThrow(new RuntimeException("SQS error"));

        // ACT & ASSERT
        assertThatThrownBy(() -> pushDeliveryHandler.send(event))
          .isInstanceOf(DeliveryException.class)
          .hasMessageContaining("Failed to send push notification");

    }

}
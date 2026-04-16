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

import com.pulsenotify.delivery.exception.DeliveryException;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.events.NotificationRequestedEvent;

import software.amazon.awssdk.services.sns.SnsClient;

@ExtendWith(MockitoExtension.class)
public class SmsDeliveryHandlerTest {
    
    @Mock
    private SnsClient snsClient;

    @InjectMocks
    private SmsDeliveryHandler smsDeliveryHandler;

    @Test
    void supports_returnTrue_forSmsChannel() {
        assertThat(smsDeliveryHandler.supports(NotificationChannel.SMS)).isTrue();
    }

    @Test
    void supports_returnFalse_forNonSmsChannel() {
        assertThat(smsDeliveryHandler.supports(NotificationChannel.EMAIL)).isFalse();
    }

    @Test
    void send_throwsDeliveryException_whenSnsClientFails() {
        // ARRANGE
        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
            .notificationId(UUID.randomUUID())
            .recipient("user@example.com")
            .channel(NotificationChannel.SMS)
            .subject("Test subject")
            .messageBody("Hello!")
            .timestamp(Instant.now())
            .build();

        when(snsClient.publish(any(software.amazon.awssdk.services.sns.model.PublishRequest.class)))    
        .thenThrow(new RuntimeException("SNS error"));

        // ACT & ASSERT
        assertThatThrownBy(() -> smsDeliveryHandler.send(event))
          .isInstanceOf(DeliveryException.class)
          .hasMessageContaining("Failed to send SMS");

    }

}
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

import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.SesClient;

@ExtendWith(MockitoExtension.class)
public class EmailDeliveryHandlerTest {
    
    @Mock
    private SesClient sesClient;

    @InjectMocks
    private EmailDeliveryHandler emailDeliveryHandler;

    @Test
    void supports_returnTrue_forEmailChannel() {
        assertThat(emailDeliveryHandler.supports(NotificationChannel.EMAIL)).isTrue();
    }

    @Test
    void supports_returnFalse_forNonEmailChannel() {
        assertThat(emailDeliveryHandler.supports(NotificationChannel.SMS)).isFalse();
    }

    @Test
    void send_throwsDeliveryException_whenSesClientFails() {
        // ARRANGE
        ReflectionTestUtils.setField(emailDeliveryHandler, "fromAddress", "noreply@pulsenotify.io");

        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
            .notificationId(UUID.randomUUID())
            .recipient("user@example.com")
            .channel(NotificationChannel.EMAIL)
            .subject("Test subject")
            .messageBody("Hello!")
            .timestamp(Instant.now())
            .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class)))    
        .thenThrow(new RuntimeException("SES error"));

        // ACT & ASSERT
        assertThatThrownBy(() -> emailDeliveryHandler.send(event))
          .isInstanceOf(DeliveryException.class)
          .hasMessageContaining("Failed to send email");

    }

}

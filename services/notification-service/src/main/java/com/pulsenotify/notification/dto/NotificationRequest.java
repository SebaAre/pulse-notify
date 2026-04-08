package com.pulsenotify.notification.dto;

import com.pulsenotify.events.NotificationChannel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(

    @NotBlank(message = "Recipient is required")
    String recipient, 
    
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    String subject, 
    
    @NotNull(message = "Notification channel is required")
    NotificationChannel channel, 
    
    @NotBlank(message = "Message body is required")
    String messageBody
)
{}

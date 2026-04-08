package com.pulsenotify.template.dto;

import com.pulsenotify.events.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TemplateRequest(

    @NotBlank(message = "Template name is required") 
    String name,
    
    @NotNull(message = "Channel is required") 
    NotificationChannel channel,
    
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    String subject,
    
    @NotBlank(message = "Template body is required") 
    String body

) {}
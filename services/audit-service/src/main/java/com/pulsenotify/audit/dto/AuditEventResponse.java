package com.pulsenotify.audit.dto;

public record AuditEventResponse(
            
        String notificationId,                                                              
        String timestamp,                                                                   
        String eventType,
        String channel,
        String recipient,
        Integer attemptNumber,
        String errorCode,
        String errorMessage,
        String providerMessageId
){}
package com.pulsenotify.audit.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pulsenotify.audit.dto.AuditEventResponse;
import com.pulsenotify.audit.model.AuditEvent;
import com.pulsenotify.audit.repository.AuditEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public void persist(AuditEvent event) {
        auditEventRepository.save(event);
    }

    public List<AuditEventResponse> getByNotificationId(String notificationId) {
        return auditEventRepository.findByNotificationId(notificationId)
                .stream()
                .map(AuditEventMapper::toResponse)
                .toList();
    }
}
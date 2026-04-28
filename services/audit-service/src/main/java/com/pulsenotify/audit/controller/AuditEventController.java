package com.pulsenotify.audit.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsenotify.audit.dto.AuditEventResponse;
import com.pulsenotify.audit.service.AuditEventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit-events")
@RequiredArgsConstructor
public class AuditEventController {

    private final AuditEventService auditEventService;

    @GetMapping("/{notificationId}")
    public ResponseEntity<List<AuditEventResponse>> getByNotificationId(@PathVariable String notificationId) {
        return ResponseEntity.ok(auditEventService.getByNotificationId(notificationId));
    }
}
package com.pulsenotify.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pulsenotify.audit.dto.AuditEventResponse;
import com.pulsenotify.audit.model.AuditEvent;
import com.pulsenotify.audit.repository.AuditEventRepository;

@ExtendWith(MockitoExtension.class)
public class AuditEventServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private AuditEventService auditEventService;

    @Test
    void persist_delegatesToRepository() {
        // ARRANGE
        AuditEvent event = AuditEvent.builder()
                .notificationId("notif-1")
                .timestamp("2026-04-28T10:00:00Z")
                .eventType("NOTIFICATION_REQUESTED")
                .channel("EMAIL")
                .recipient("user@example.com")
                .build();

        // ACT
        auditEventService.persist(event);

        // ASSERT
        verify(auditEventRepository).save(event);
    }

    @Test
    void getByNotificationId_returnsMappedList() {
        // ARRANGE
        String notificationId = "notif-1";
        AuditEvent e1 = AuditEvent.builder()
                .notificationId(notificationId)
                .timestamp("2026-04-28T10:00:00Z")
                .eventType("NOTIFICATION_REQUESTED")
                .channel("EMAIL")
                .recipient("user@example.com")
                .build();

        AuditEvent e2 = AuditEvent.builder()
                .notificationId(notificationId)
                .timestamp("2026-04-28T10:00:05Z")
                .eventType("DELIVERY_COMPLETED")
                .channel("EMAIL")
                .recipient("user@example.com")
                .providerMessageId("ses-msg-123")
                .build();

        when(auditEventRepository.findByNotificationId(notificationId))
                .thenReturn(List.of(e1, e2));

        // ACT
        List<AuditEventResponse> responses = auditEventService.getByNotificationId(notificationId);

        // ASSERT
        assertThat(responses).hasSize(2);
        assertThat(responses).allMatch(r -> r.notificationId().equals(notificationId));
        assertThat(responses.get(0).eventType()).isEqualTo("NOTIFICATION_REQUESTED");
        assertThat(responses.get(1).providerMessageId()).isEqualTo("ses-msg-123");
    }

    @Test
    void getByNotificationId_returnsEmptyListWhenNoEvents() {
        // ARRANGE
        String notificationId = "notif-unknown";
        when(auditEventRepository.findByNotificationId(notificationId))
                .thenReturn(List.of());

        // ACT
        List<AuditEventResponse> responses = auditEventService.getByNotificationId(notificationId);

        // ASSERT
        assertThat(responses).isEmpty();
    }
}
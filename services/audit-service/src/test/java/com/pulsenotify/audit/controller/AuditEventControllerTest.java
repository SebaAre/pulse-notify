package com.pulsenotify.audit.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pulsenotify.audit.dto.AuditEventResponse;
import com.pulsenotify.audit.service.AuditEventService;

@WebMvcTest(AuditEventController.class)
class AuditEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditEventService auditEventService;

    @Test
    void getByNotificationId_returns200WithList() throws Exception {
        String notificationId = "notif-1";

        List<AuditEventResponse> responses = List.of(
                new AuditEventResponse(
                        notificationId,
                        "2026-04-28T10:00:00Z",
                        "NOTIFICATION_REQUESTED",
                        "EMAIL",
                        "user@example.com",
                        null, null, null, null
                ),
                new AuditEventResponse(
                        notificationId,
                        "2026-04-28T10:00:05Z",
                        "DELIVERY_COMPLETED",
                        "EMAIL",
                        "user@example.com",
                        null, null, null, "ses-msg-123"
                )
        );

        when(auditEventService.getByNotificationId(notificationId)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/audit-events/{notificationId}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notificationId").value(notificationId))
                .andExpect(jsonPath("$[0].eventType").value("NOTIFICATION_REQUESTED"))
                .andExpect(jsonPath("$[1].providerMessageId").value("ses-msg-123"));
    }

    @Test
    void getByNotificationId_returns200WithEmptyListWhenNoEvents() throws Exception {
        String notificationId = "notif-unknown";

        when(auditEventService.getByNotificationId(notificationId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit-events/{notificationId}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
package com.pulsenotify.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.notification.dto.NotificationRequest;
import com.pulsenotify.notification.dto.NotificationResponse;
import com.pulsenotify.notification.exception.NotificationNotFoundException;
import com.pulsenotify.notification.model.NotificationStatus;
import com.pulsenotify.notification.service.NotificationService;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendNotification_returns201WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationRequest request = new NotificationRequest(
                "user@example.com",
                "Test subject",
                NotificationChannel.EMAIL,
                "Hello!"
        );

        NotificationResponse response = new NotificationResponse(
                id,
                "user@example.com",
                "Test subject",
                NotificationChannel.EMAIL,
                "Hello!",
                NotificationStatus.PENDING,
                now,
                now
        );

        when(notificationService.sendNotification(any(NotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.recipient").value("user@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void sendNotification_returns400WhenBodyIsInvalid() throws Exception {
        NotificationRequest invalidRequest = new NotificationRequest(
                "",
                "Hi",
                null,
                ""
        );

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getNotificationById_returns200WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationResponse response = new NotificationResponse(
                id,
                "user@example.com",
                "Test subject",
                NotificationChannel.EMAIL,
                "Hello!",
                NotificationStatus.PENDING,
                now,
                now
        );

        when(notificationService.getNotificationById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.recipient").value("user@example.com"));
    }

    @Test
    void getNotificationById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(notificationService.getNotificationById(id))
                .thenThrow(new NotificationNotFoundException(id));

        mockMvc.perform(get("/api/v1/notifications/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getNotificationsByRecipient_returns200WithList() throws Exception {
        String recipient = "user@example.com";
        Instant now = Instant.now();

        List<NotificationResponse> responses = List.of(
                new NotificationResponse(UUID.randomUUID(), recipient, "Subject 1",
                        NotificationChannel.EMAIL, "Body 1", NotificationStatus.PENDING, now, now),
                new NotificationResponse(UUID.randomUUID(), recipient, "Subject 2",
                        NotificationChannel.EMAIL, "Body 2", NotificationStatus.PENDING, now, now)
        );

        when(notificationService.getNotificationsByRecipient(eq(recipient))).thenReturn(responses);

        mockMvc.perform(get("/api/v1/notifications").param("recipient", recipient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].recipient").value(recipient));
    }
}
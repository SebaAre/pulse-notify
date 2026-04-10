package com.pulsenotify.template.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.template.dto.TemplateRenderRequest;
import com.pulsenotify.template.dto.TemplateRequest;
import com.pulsenotify.template.dto.TemplateResponse;
import com.pulsenotify.template.exception.TemplateNotFoundException;
import com.pulsenotify.template.service.TemplateService;

@WebMvcTest(TemplateController.class)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/templates";


    private TemplateResponse buildResponse(UUID id, String name) {
        return new TemplateResponse(
                id,
                name,
                NotificationChannel.EMAIL,
                "Subject",
                "Hello ${name}",
                Instant.now(),
                Instant.now()
        );
    }

    private TemplateRequest buildRequest(String name) {
        return new TemplateRequest(name, NotificationChannel.EMAIL, "Subject", "Hello ${name}");
    }

    @Test
    void create_returns201WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        TemplateRequest request = buildRequest("order-confirmation");
        TemplateResponse response = buildResponse(id, "order-confirmation");

        when(templateService.create(any(TemplateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("order-confirmation"))
                .andExpect(jsonPath("$.channel").value("EMAIL"));
    }

    @Test
    void create_returns400WhenBodyIsInvalid() throws Exception {
        TemplateRequest invalid = new TemplateRequest("", NotificationChannel.EMAIL, "Subject", "body");

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void findAll_returns200WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(templateService.findAll()).thenReturn(List.of(
                buildResponse(id1, "template-one"),
                buildResponse(id2, "template-two")
        ));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("template-one"))
                .andExpect(jsonPath("$[1].name").value("template-two"));
    }

    @Test
    void findById_returns200WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(templateService.findById(id)).thenReturn(buildResponse(id, "welcome"));

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("welcome"));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(templateService.findById(id)).thenThrow(new TemplateNotFoundException(id));

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Template not found with id: " + id));
    }

    @Test
    void update_returns200WithUpdatedBody() throws Exception {
        UUID id = UUID.randomUUID();
        TemplateRequest request = buildRequest("updated-name");
        TemplateResponse response = buildResponse(id, "updated-name");

        when(templateService.update(eq(id), any(TemplateRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-name"));
    }

    @Test
    void delete_returns204WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(templateService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void render_returns200WithRenderedString() throws Exception {
        UUID id = UUID.randomUUID();
        TemplateRenderRequest request = new TemplateRenderRequest(Map.of("name", "Sebastian"));

        when(templateService.render(eq(id), any())).thenReturn("Hello Sebastian!");

        mockMvc.perform(post(BASE_URL + "/" + id + "/render")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Sebastian!"));
    }
}
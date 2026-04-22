package com.pulsenotify.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsenotify.user.dto.CreateUserRequest;
import com.pulsenotify.user.dto.UserResponse;
import com.pulsenotify.user.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_returns201WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        CreateUserRequest request = new CreateUserRequest(
                "user@example.com",
                "+54911111111",
                "push-token-123",
                "Sebastian Arellano",
                "America/Argentina/Buenos_Aires"
        );

        UserResponse response = new UserResponse(
                id,
                "user@example.com",
                "+54911111111",
                "Sebastian Arellano",
                "America/Argentina/Buenos_Aires",
                true,
                now,
                now
        );

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.active").value(true));
    }
}
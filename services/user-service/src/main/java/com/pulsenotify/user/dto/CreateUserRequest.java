package com.pulsenotify.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    String email,

    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    String phone,

    @Size(max = 254, message = "Push token must not exceed 254 characters")
    String pushToken,
    
    @NotBlank(message = "Display name is required")
    @Size(max = 254, message = "Display name must not exceed 254 characters")
    String displayName,

    @NotBlank(message = "Timezone is required")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    String timezone

){}
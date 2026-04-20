package com.pulsenotify.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(

    UUID id,

    String email,

    String phone,

    String displayName,

    String timezone,

    Boolean active,

    Instant createdAt,

    Instant updatedAt

) {}
package com.pulsenotify.template.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record TemplateRenderRequest(

    @NotNull(message = "Variables map is required")
    Map<String, Object> variables
    
) {}
package com.pulsenotify.template.exception;

import java.util.UUID;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(UUID id) {
        super("Template not found with id: " + id);
    }
}
package com.pulsenotify.gateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsenotify.gateway")
public record GatewayProperties(
    Cors cors,
    Downstream downstream
) {
    public record Cors(List<String> allowedOrigins) {}

    public record Downstream(
        String notification,
        String template,
        String user,
        String audit
    ) {}
}
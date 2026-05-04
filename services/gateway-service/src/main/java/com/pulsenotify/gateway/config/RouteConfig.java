package com.pulsenotify.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class RouteConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, GatewayProperties props) {
        var ds = props.downstream();
        return builder.routes()
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri(ds.notification()))
                .route("template-service", r -> r
                        .path("/api/templates/**")
                        .uri(ds.template()))
                .route("user-service", r -> r
                        .path("/api/users/**", "/api/preferences/**")
                        .uri(ds.user()))
                .route("audit-service", r -> r
                        .path("/api/audit/**")
                        .uri(ds.audit()))
                .build();
    }
}
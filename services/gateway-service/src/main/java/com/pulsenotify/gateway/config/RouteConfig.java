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
                        .filters(f -> f.rewritePath("/api/notifications(?<segment>/.*)?", "/api/v1/notifications$\\{segment}"))
                        .uri(ds.notification()))
                .route("template-service", r -> r
                        .path("/api/templates/**")
                        .filters(f -> f.rewritePath("/api/templates(?<segment>/.*)?", "/api/v1/templates$\\{segment}"))
                        .uri(ds.template()))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.rewritePath("/api/users(?<segment>/.*)?", "/api/v1/users$\\{segment}"))
                        .uri(ds.user()))
                .route("audit-service", r -> r
                        .path("/api/audit/**")
                        .filters(f -> f.rewritePath("/api/audit(?<segment>/.*)?", "/api/v1/audit-events$\\{segment}"))
                        .uri(ds.audit()))
                .build();
    }
}
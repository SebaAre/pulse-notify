package com.pulsenotify.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var addr = exchange.getRequest().getRemoteAddress();
            String key = addr != null ? addr.getAddress().getHostAddress() : "unknown";
            return Mono.just(key);
        };
    }
}
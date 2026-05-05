package com.pulsenotify.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

class RateLimitConfigTest {

    private final KeyResolver resolver = new RateLimitConfig().ipKeyResolver();

    @Test
    void resolve_withRemoteAddress_returnsIp() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/notifications")
                .remoteAddress(new InetSocketAddress("203.0.113.42", 51000))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("203.0.113.42"))
                .verifyComplete();
    }

    @Test
    void resolve_withoutRemoteAddress_returnsUnknown() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/notifications").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("unknown"))
                .verifyComplete();
    }
}
package com.pulsenotify.gateway.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void resolveStatus_invalidBearerToken_returns401() {
        HttpStatus status = handler.resolveStatus(new InvalidBearerTokenException("bad token"));

        assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void resolveStatus_notFoundException_returns503() {
        HttpStatus status = handler.resolveStatus(NotFoundException.create(true, "no instance"));

        assertThat(status).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void resolveStatus_responseStatusException_returnsThatStatus() {
        HttpStatus status = handler.resolveStatus(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid"));

        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resolveStatus_genericException_returns500() {
        HttpStatus status = handler.resolveStatus(new RuntimeException("boom"));

        assertThat(status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handle_writesJsonBodyWithStatusAndPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/notifications/123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(handler.handle(exchange, new InvalidBearerTokenException("nope")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_JSON);

        StepVerifier.create(exchange.getResponse().getBodyAsString())
                .assertNext(body -> assertThat(body)
                        .contains("\"status\":401")
                        .contains("\"error\":\"Unauthorized\"")
                        .contains("\"message\":\"nope\"")
                        .contains("\"path\":\"/api/notifications/123\""))
                .verifyComplete();
    }

    @Test
    void handle_nullMessage_fallsBackToReasonPhrase() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/foo").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(handler.handle(exchange, new RuntimeException()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        StepVerifier.create(exchange.getResponse().getBodyAsString())
                .assertNext(body -> assertThat(body)
                        .contains("\"message\":\"Internal Server Error\""))
                .verifyComplete();
    }
}
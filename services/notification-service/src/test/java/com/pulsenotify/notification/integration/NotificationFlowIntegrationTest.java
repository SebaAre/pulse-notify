package com.pulsenotify.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.pulsenotify.events.DeliveryCompletedEvent;
import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.notification.dto.NotificationRequest;
import com.pulsenotify.notification.dto.NotificationResponse;
import com.pulsenotify.notification.model.NotificationStatus;
import com.pulsenotify.notification.repository.NotificationRepository;

@Tag("integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

    @Container
    static final KafkaContainer kafka =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void deliveryCompletedEvent_flipsNotificationStatusToSent() {
        NotificationRequest request = new NotificationRequest(
            "user@example.com",
            "Integration test",
            NotificationChannel.EMAIL,
            "Body"
        );

        ResponseEntity<NotificationResponse> response = restTemplate.postForEntity(
            "/api/v1/notifications", request, NotificationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        NotificationResponse created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.status()).isEqualTo(NotificationStatus.PENDING);

        DeliveryCompletedEvent event = DeliveryCompletedEvent.builder()
            .notificationId(created.id())
            .channel(NotificationChannel.EMAIL)
            .recipient(created.recipient())
            .timestamp(Instant.now())
            .build();

        testKafkaTemplate().send("delivery.completed", created.id().toString(), event);

        await().atMost(Duration.ofSeconds(20))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> assertThat(notificationRepository.findById(created.id()))
                .get()
                .extracting("status")
                .isEqualTo(NotificationStatus.SENT));
    }

    private KafkaTemplate<String, Object> testKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(factory);
    }
}

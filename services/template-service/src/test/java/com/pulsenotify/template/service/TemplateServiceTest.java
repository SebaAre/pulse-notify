package com.pulsenotify.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pulsenotify.events.NotificationChannel;
import com.pulsenotify.template.dto.TemplateRequest;
import com.pulsenotify.template.dto.TemplateResponse;
import com.pulsenotify.template.exception.TemplateNotFoundException;
import com.pulsenotify.template.model.NotificationTemplate;
import com.pulsenotify.template.repository.TemplateRepository;

import freemarker.template.Configuration;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Spy
    private Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);

    @InjectMocks
    private TemplateService templateService;

    @Test
    void create_savesAndReturnsTemplateResponse() {
        // ARRANGE
        TemplateRequest request = new TemplateRequest(
            "order-confirmation", 
            NotificationChannel.EMAIL, 
            "Order Confirmed", 
            "Hello ${name}"
        );

        NotificationTemplate savedTemplate = NotificationTemplate.builder()
            .id(UUID.randomUUID())
            .name("order-confirmation")
            .channel(NotificationChannel.EMAIL)
            .subject("Order Confirmed")
            .body("Hello ${name}")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(savedTemplate);

        // ACT
        TemplateResponse response = templateService.create(request);

        // ASSERT
        assertThat(response.name()).isEqualTo("order-confirmation");
        assertThat(response.channel()).isEqualTo(NotificationChannel.EMAIL);
        verify(templateRepository).save(any(NotificationTemplate.class));
    }

    @Test
    void findById_returnsResponseWhenFound() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        NotificationTemplate template = NotificationTemplate.builder()
            .id(id)
            .name("order-confirmation")
            .body("Hello ${name}")
            .build();

        when(templateRepository.findById(id)).thenReturn(Optional.of(template));

        // ACT
        TemplateResponse response = templateService.findById(id);

        // ASSERT
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("order-confirmation");
    }

    @Test
    void findById_throwsWhenNotFound() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        when(templateRepository.findById(id)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> templateService.findById(id))
            .isInstanceOf(TemplateNotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    @Test
    void findAll_returnsMappedList() {
        // ARRANGE
        NotificationTemplate t1 = NotificationTemplate.builder()
            .name("template-1")
            .build();
        
        when(templateRepository.findAll()).thenReturn(List.of(t1));

        // ACT
        List<TemplateResponse> responses = templateService.findAll();

        // ASSERT
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("template-1");
    }

    @Test
    void update_mutatesFieldsAndReturnsResponse() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        NotificationTemplate existingTemplate = NotificationTemplate.builder()
            .id(id)
            .name("old-name")
            .build();

        TemplateRequest updateRequest = new TemplateRequest(
            "new-name", 
            NotificationChannel.SMS, 
            "New Subject", 
            "Hi"
        );

        when(templateRepository.findById(id)).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(existingTemplate);

        // ACT
        TemplateResponse response = templateService.update(id, updateRequest);

        // ASSERT
        assertThat(existingTemplate.getName()).isEqualTo("new-name");
        assertThat(response.name()).isEqualTo("new-name");
        verify(templateRepository).save(existingTemplate);
    }

    @Test
    void delete_callsRepositoryWhenFound() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        when(templateRepository.existsById(id)).thenReturn(true);

        // ACT
        templateService.delete(id);

        // ASSERT
        verify(templateRepository).deleteById(id);
    }

    @Test
    void render_returnsProcessedString() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        NotificationTemplate template = NotificationTemplate.builder()
            .id(id)
            .body("Hello ${name}!")
            .build();

        Map<String, Object> variables = Map.of("name", "Sebastian");
        when(templateRepository.findById(id)).thenReturn(Optional.of(template));

        // ACT
        String result = templateService.render(id, variables);

        // ASSERT
        assertThat(result).isEqualTo("Hello Sebastian!");
    }
}
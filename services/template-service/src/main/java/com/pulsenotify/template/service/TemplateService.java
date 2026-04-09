package com.pulsenotify.template.service;

import com.pulsenotify.template.dto.TemplateRequest;
import com.pulsenotify.template.dto.TemplateResponse;
import com.pulsenotify.template.exception.TemplateNotFoundException;
import com.pulsenotify.template.model.NotificationTemplate;
import com.pulsenotify.template.repository.TemplateRepository;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final Configuration freemarkerConfig;

    @Transactional
    public TemplateResponse create(TemplateRequest request) {
        NotificationTemplate template = NotificationTemplate.builder()
                .name(request.name())
                .channel(request.channel())
                .subject(request.subject())
                .body(request.body())
                .build();

        return toResponse(templateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public TemplateResponse findById(UUID id) {
        return templateRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TemplateNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> findAll() {
        return templateRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TemplateResponse update(UUID id, TemplateRequest request) {
        NotificationTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id));

        existing.setName(request.name());
        existing.setChannel(request.channel());
        existing.setSubject(request.subject());
        existing.setBody(request.body());

        return toResponse(templateRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        if (!templateRepository.existsById(id)) {
            throw new TemplateNotFoundException(id);
        }
        templateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String render(UUID id, Map<String, Object> variables) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException(id));

        try {
            Template freemarkerTemplate = new Template(id.toString(), template.getBody(), freemarkerConfig);
            
            StringWriter writer = new StringWriter();
            freemarkerTemplate.process(variables, writer);
            
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render template: " + e.getMessage(), e);
        }
    }


    private TemplateResponse toResponse(NotificationTemplate template) {
        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getChannel(),
                template.getSubject(),
                template.getBody(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
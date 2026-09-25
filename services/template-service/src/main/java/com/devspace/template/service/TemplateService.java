package com.devspace.template.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devspace.template.dto.request.CreateTemplateRequest;
import com.devspace.template.dto.response.TemplateResponse;
import com.devspace.template.model.EnvironmentTemplate;
import com.devspace.template.repository.EnvironmentTemplateRepository;

@Service
public class TemplateService {

    @Autowired
    private EnvironmentTemplateRepository templateRepository;

    // Create Template
    public TemplateResponse createTemplate(CreateTemplateRequest request) {

        EnvironmentTemplate template = convertToEntity(request);

        EnvironmentTemplate savedTemplate =
                templateRepository.save(template);

        return convertToDTO(savedTemplate);
    }

    // Get Template By ID
    public TemplateResponse getTemplateById(String templateId) {

        EnvironmentTemplate template =
                templateRepository.findById(templateId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Template not found with id: " + templateId
                                )
                        );

        return convertToDTO(template);
    }

    // Get All Templates
    public List<TemplateResponse> getAllTemplates() {

        List<EnvironmentTemplate> templates =
                templateRepository.findAll();

        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get Active Templates
    public List<TemplateResponse> getActiveTemplates() {

        List<EnvironmentTemplate> templates =
                templateRepository.findByActiveTrue();

        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Convert DTO to Entity
    private EnvironmentTemplate convertToEntity(
            CreateTemplateRequest request) {

        EnvironmentTemplate template = new EnvironmentTemplate();

        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setRuntimeLanguage(request.getRuntimeLanguage());
        template.setRuntimeVersion(request.getRuntimeVersion());
        template.setApplicationPort(request.getApplicationPort());
        template.setDatabaseType(request.getDatabaseType());
        template.setRedisEnabled(request.getRedisEnabled());
        template.setKafkaEnabled(request.getKafkaEnabled());
        template.setCpuRequest(request.getCpuRequest());
        template.setCpuLimit(request.getCpuLimit());
        template.setMemoryRequest(request.getMemoryRequest());
        template.setMemoryLimit(request.getMemoryLimit());

        return template;
    }

    // Convert Entity to DTO
    private TemplateResponse convertToDTO(
            EnvironmentTemplate template) {

        return new TemplateResponse(
                template.getTemplateId(),
                template.getName(),
                template.getDescription(),
                template.getRuntimeLanguage(),
                template.getRuntimeVersion(),
                template.getApplicationPort(),
                template.getDatabaseType(),
                template.getRedisEnabled(),
                template.getKafkaEnabled(),
                template.getCpuRequest(),
                template.getCpuLimit(),
                template.getMemoryRequest(),
                template.getMemoryLimit(),
                template.getActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
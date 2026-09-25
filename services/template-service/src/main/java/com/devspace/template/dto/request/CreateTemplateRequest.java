package com.devspace.template.dto.request;

import com.devspace.template.model.DatabaseType;
import com.devspace.template.model.RuntimeLanguage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @NotNull(message = "Runtime language is required")
    private RuntimeLanguage runtimeLanguage;

    @NotBlank(message = "Runtime version is required")
    private String runtimeVersion;

    @NotNull(message = "Application port is required")
    @Positive(message = "Application port must be greater than 0")
    private Integer applicationPort;

    @NotNull(message = "Database type is required")
    private DatabaseType databaseType;

    private Boolean redisEnabled;

    private Boolean kafkaEnabled;

    @NotBlank(message = "CPU request is required")
    private String cpuRequest;

    @NotBlank(message = "CPU limit is required")
    private String cpuLimit;

    @NotBlank(message = "Memory request is required")
    private String memoryRequest;

    @NotBlank(message = "Memory limit is required")
    private String memoryLimit;
}
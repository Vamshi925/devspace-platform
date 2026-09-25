package com.devspace.environment.dto.request;

import com.devspace.environment.model.EnvironmentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEnvironmentRequest {

    @NotBlank(message = "Application name is required")
    private String applicationName;

    @NotNull(message = "Template ID is required")
    private Long templateId;

    @NotNull(message = "Environment type is required")
    private EnvironmentType environmentType;

    @NotNull(message = "Lifetime is required")
    @Positive(message = "Lifetime must be greater than 0")
    private Integer lifetimeHours;

    private String repositoryUrl;

    private String branchName;
}

package com.devspace.provisioning.dto.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvisioningRequest {

    @NotBlank(message = "Environment ID is required")
    private String environmentId;

    @NotBlank(message = "Environment code is required")
    private String environmentCode;

    @NotBlank(message = "Application name is required")
    private String applicationName;

    @NotBlank(message = "Template ID is required")
    private String templateId;

    @NotNull(message = "Expiration time is required")
    private Instant expiresAt;

    private String repositoryUrl;

    private String branchName;
}
package com.devspace.environment.dto.response;

import java.time.Instant;

import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.model.EnvironmentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentResponse {

    private String environmentId;

    private String environmentCode;

    private String applicationName;

    private String userId;

    private String templateId;
    
    private EnvironmentType environmentType;

    private EnvironmentStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant expiresAt;

    private String namespace;

    private String applicationUrl;

    private String repositoryUrl;

    private String branchName;

    private String failureReason;

    private String provisioningStage;
}

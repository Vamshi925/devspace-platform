package com.devspace.environment.dto.request;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvisioningRequest {

    private String environmentId;
    private String environmentCode;
    private String applicationName;
    private String templateId;
    private Instant expiresAt;
    private String repositoryUrl;
    private String branchName;
}
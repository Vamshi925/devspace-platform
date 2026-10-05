package com.devspace.provisioning.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeprovisioningRequest {

    @NotBlank(message = "Environment ID is required")
    private String environmentId;

    @NotBlank(message = "Environment code is required")
    private String environmentCode;
}
package com.devspace.provisioning.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvisioningStatusRequest {

    private String status;
    private String namespace;
    private String applicationUrl;
    private String failureReason;
}
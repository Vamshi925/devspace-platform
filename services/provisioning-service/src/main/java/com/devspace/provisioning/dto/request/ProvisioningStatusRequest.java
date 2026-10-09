package com.devspace.provisioning.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningStatusRequest {

    private String status;
    private String stage;
    private String namespace;
    private String applicationUrl;
    private String failureReason;
}
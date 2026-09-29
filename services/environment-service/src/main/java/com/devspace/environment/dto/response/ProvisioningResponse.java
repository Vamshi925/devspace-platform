package com.devspace.environment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvisioningResponse {

    private String environmentId;
    private String status;
    private String message;
}
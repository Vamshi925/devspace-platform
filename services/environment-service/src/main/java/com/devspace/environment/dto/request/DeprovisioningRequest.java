package com.devspace.environment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeprovisioningRequest {

    private String environmentId;
    private String environmentCode;
}
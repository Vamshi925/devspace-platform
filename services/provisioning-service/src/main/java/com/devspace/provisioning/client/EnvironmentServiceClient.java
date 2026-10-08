package com.devspace.provisioning.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.devspace.provisioning.dto.request.ProvisioningStatusRequest;

@Component
public class EnvironmentServiceClient {

    private final RestClient restClient;

    @Value("${devspace.internal.api-key}")
    private String internalApiKey;  

    public EnvironmentServiceClient(
            @Value("${devspace.services.environment.url}") String environmentServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(environmentServiceUrl)
                .build();
    }

    public void updateProvisioningStatus(
            String environmentId,
            ProvisioningStatusRequest request) {

        restClient.patch()
                .uri(
                        "/internal/environments/{environmentId}/provisioning-status",
                        environmentId
                )
                .header(
                        "X-Internal-Api-Key",
                        internalApiKey
                )
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
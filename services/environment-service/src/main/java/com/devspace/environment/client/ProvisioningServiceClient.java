package com.devspace.environment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.devspace.environment.dto.request.DeprovisioningRequest;
import com.devspace.environment.dto.request.ProvisioningRequest;
import com.devspace.environment.dto.response.ProvisioningResponse;
import com.devspace.environment.exception.ProvisioningServiceUnavailableException;

@Component
public class ProvisioningServiceClient {

    private final RestClient restClient;

    @Value("${devspace.internal.api-key}")
    private String internalApiKey;

    public ProvisioningServiceClient(
            @Value("${devspace.services.provisioning.url}")
            String provisioningServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(provisioningServiceUrl)
                .build();
    }

    public ProvisioningResponse provisionEnvironment(
            ProvisioningRequest request) {

        try {

            return restClient
                    .post()
                    .uri("/api/provisioning")
                    .header(
                        "X-Internal-Api-Key",
                        internalApiKey
                    )
                    .body(request)
                    .retrieve()
                    .body(ProvisioningResponse.class);

        } catch (HttpServerErrorException ex) {

            throw new ProvisioningServiceUnavailableException(
                    "Provisioning Service returned an internal error"
            );

        } catch (ResourceAccessException ex) {

            throw new ProvisioningServiceUnavailableException(
                    "Unable to communicate with Provisioning Service"
            );
        }
    }

    public ProvisioningResponse deprovisionEnvironment(
            DeprovisioningRequest request) {

        try {

            return restClient
                    .method(HttpMethod.DELETE)
                    .uri("/api/provisioning")
                    .header(
                        "X-Internal-Api-Key",
                        internalApiKey
                    )
                    .body(request)
                    .retrieve()
                    .body(ProvisioningResponse.class);

        } catch (HttpServerErrorException ex) {

            throw new ProvisioningServiceUnavailableException(
                    "Provisioning Service returned server error"
            );

        } catch (ResourceAccessException ex) {

            throw new ProvisioningServiceUnavailableException(
                    "Provisioning Service is unavailable"
            );
        }
    }
}
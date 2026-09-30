package com.devspace.provisioning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.devspace.provisioning.client.EnvironmentServiceClient;
import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.dto.request.ProvisioningStatusRequest;
import com.devspace.provisioning.dto.response.ProvisioningResponse;

@Service
public class ProvisioningService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProvisioningService.class);

    private final ProvisioningOrchestrator provisioningOrchestrator;
    private final EnvironmentServiceClient environmentServiceClient;

    public ProvisioningService(
            ProvisioningOrchestrator provisioningOrchestrator,
            EnvironmentServiceClient environmentServiceClient) {

        this.provisioningOrchestrator = provisioningOrchestrator;
        this.environmentServiceClient = environmentServiceClient;
    }

    public ProvisioningResponse provisionEnvironment(
            ProvisioningRequest request) {

        logger.info(
                "Provisioning request received - EnvironmentId: {}, EnvironmentCode: {}, TemplateId: {}",
                request.getEnvironmentId(),
                request.getEnvironmentCode(),
                request.getTemplateId()
        );

        try {

            String namespace =
                    provisioningOrchestrator.provisionEnvironment(
                            request
                    );

            ProvisioningStatusRequest statusRequest =
                    new ProvisioningStatusRequest(
                            "READY",
                            namespace,
                            null,
                            null
                    );

            updateEnvironmentStatusSafely(
                    request.getEnvironmentId(),
                    statusRequest
            );

            return new ProvisioningResponse(
                    request.getEnvironmentId(),
                    "ACCEPTED",
                    "Provisioning request accepted"
            );

        } catch (Exception ex) {

            logger.error(
                    "Provisioning failed - EnvironmentId: {}",
                    request.getEnvironmentId(),
                    ex
            );

            ProvisioningStatusRequest statusRequest =
                    new ProvisioningStatusRequest(
                            "FAILED",
                            null,
                            null,
                            ex.getMessage()
                    );

            updateEnvironmentStatusSafely(
                    request.getEnvironmentId(),
                    statusRequest
            );

            return new ProvisioningResponse(
                    request.getEnvironmentId(),
                    "FAILED",
                    "Provisioning failed"
            );
        }
    }

    private void updateEnvironmentStatusSafely(
            String environmentId,
            ProvisioningStatusRequest statusRequest) {

        try {

            environmentServiceClient.updateProvisioningStatus(
                    environmentId,
                    statusRequest
            );

        } catch (Exception ex) {

            logger.error(
                    "Failed to update Environment Service status - EnvironmentId: {}, Status: {}",
                    environmentId,
                    statusRequest.getStatus(),
                    ex
            );
        }
    }
}
package com.devspace.provisioning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devspace.provisioning.client.EnvironmentServiceClient;
import com.devspace.provisioning.dto.request.DeprovisioningRequest;
import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.dto.request.ProvisioningStatusRequest;
import com.devspace.provisioning.dto.response.ProvisioningResponse;
import com.devspace.provisioning.kubernetes.EnvironmentCleanupProvisioner;
import com.devspace.provisioning.kubernetes.NamespaceDeletionChecker;

@Service
public class ProvisioningService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProvisioningService.class);

    @Value("${devspace.ingress.base-url}")
    private String ingressBaseUrl;

    private final ProvisioningOrchestrator provisioningOrchestrator;
    private final EnvironmentServiceClient environmentServiceClient;
    private final EnvironmentCleanupProvisioner environmentCleanupProvisioner;
    private final NamespaceDeletionChecker namespaceDeletionChecker;

    public ProvisioningService(
            ProvisioningOrchestrator provisioningOrchestrator,
            EnvironmentServiceClient environmentServiceClient,
            EnvironmentCleanupProvisioner environmentCleanupProvisioner,
            NamespaceDeletionChecker namespaceDeletionChecker) {

        this.provisioningOrchestrator = provisioningOrchestrator;
        this.environmentServiceClient = environmentServiceClient;
        this.environmentCleanupProvisioner = environmentCleanupProvisioner;
        this.namespaceDeletionChecker = namespaceDeletionChecker;
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

            String applicationUrl =
                    ingressBaseUrl
                            + "/"
                            + request.getEnvironmentCode();

            ProvisioningStatusRequest statusRequest =
                    new ProvisioningStatusRequest(
                            "READY",
                            "READY",
                            namespace,
                            applicationUrl,
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

    public ProvisioningResponse deprovisionEnvironment(
            DeprovisioningRequest request) {

        logger.info(
                "Deprovisioning request received - EnvironmentId: {}, EnvironmentCode: {}",
                request.getEnvironmentId(),
                request.getEnvironmentCode()
        );

        String namespace =
                "devspace-" + request.getEnvironmentCode();

        try {

            environmentCleanupProvisioner.deleteEnvironment(
                    request.getEnvironmentCode()
            );

            namespaceDeletionChecker.waitUntilDeleted(
                    namespace
            );

            ProvisioningStatusRequest statusRequest =
                    new ProvisioningStatusRequest(
                            "DELETED",
                            null,
                            null,
                            null,
                            null
                    );

            updateEnvironmentStatusSafely(
                    request.getEnvironmentId(),
                    statusRequest
            );

            return new ProvisioningResponse(
                    request.getEnvironmentId(),
                    "DELETED",
                    "Environment cleanup completed"
            );

        } catch (Exception ex) {

            logger.error(
                    "Environment cleanup failed - EnvironmentId: {}",
                    request.getEnvironmentId(),
                    ex
            );

            ProvisioningStatusRequest statusRequest =
                    new ProvisioningStatusRequest(
                            "FAILED",
                            null,
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
                    "Environment cleanup failed"
            );
        }
    }
}
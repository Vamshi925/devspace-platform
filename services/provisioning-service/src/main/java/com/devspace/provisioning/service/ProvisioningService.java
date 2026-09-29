package com.devspace.provisioning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.dto.response.ProvisioningResponse;
import com.devspace.provisioning.dto.request.ProvisioningStatusRequest;
import com.devspace.provisioning.client.EnvironmentServiceClient;
import com.devspace.provisioning.kubernetes.NamespaceProvisioner;

@Service
public class ProvisioningService {

    private final NamespaceProvisioner namespaceProvisioner;
    private final EnvironmentServiceClient environmentServiceClient;

  public ProvisioningService(
        NamespaceProvisioner namespaceProvisioner,
        EnvironmentServiceClient environmentServiceClient) {

    this.namespaceProvisioner = namespaceProvisioner;
    this.environmentServiceClient = environmentServiceClient;
}

    private static final Logger logger =
            LoggerFactory.getLogger(ProvisioningService.class);

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
                namespaceProvisioner.createNamespace(
                        request.getEnvironmentCode()
                );

        ProvisioningStatusRequest statusRequest =
                new ProvisioningStatusRequest(
                        "READY",
                        namespace,
                        null,
                        null
                );

        environmentServiceClient.updateProvisioningStatus(
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

        environmentServiceClient.updateProvisioningStatus(
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
}
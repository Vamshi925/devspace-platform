package com.devspace.provisioning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.devspace.provisioning.client.EnvironmentServiceClient;
import com.devspace.provisioning.dto.request.ProvisioningStatusRequest;

@Component
public class ProvisioningProgressReporter {

    private static final Logger logger =
            LoggerFactory.getLogger(ProvisioningProgressReporter.class);

    private final EnvironmentServiceClient environmentServiceClient;

    public ProvisioningProgressReporter(
            EnvironmentServiceClient environmentServiceClient) {
        this.environmentServiceClient = environmentServiceClient;
    }

    public void report(String environmentId, String stage) {
        try {
            environmentServiceClient.updateProvisioningStatus(
                    environmentId,
                    new ProvisioningStatusRequest(
                            "PROVISIONING",
                            stage,
                            null,
                            null,
                            null
                    )
            );
        } catch (Exception ex) {
            logger.warn(
                    "Failed to report provisioning stage - EnvironmentId: {}, Stage: {}",
                    environmentId,
                    stage
            );
        }
    }
}
package com.devspace.provisioning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devspace.provisioning.client.EnvironmentServiceClient;
import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.dto.request.ProvisioningStatusRequest;
import com.devspace.provisioning.dto.response.ProvisioningResponse;
import com.devspace.provisioning.dto.request.DeprovisioningRequest;
import com.devspace.provisioning.kubernetes.EnvironmentCleanupProvisioner;
import com.devspace.provisioning.kubernetes.NamespaceDeletionChecker;

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceTest {

    @Mock
    private ProvisioningOrchestrator provisioningOrchestrator;

    @Mock
    private EnvironmentServiceClient environmentServiceClient;

    @InjectMocks
    private ProvisioningService provisioningService;

    private ProvisioningRequest provisioningRequest;

    @Mock
    private EnvironmentCleanupProvisioner environmentCleanupProvisioner;

    @Mock
    private NamespaceDeletionChecker namespaceDeletionChecker;

    @BeforeEach
    void setUp() {

        provisioningRequest = new ProvisioningRequest();

        provisioningRequest.setEnvironmentId("env-123");
        provisioningRequest.setEnvironmentCode("payment-service-a1234");
        provisioningRequest.setApplicationName("payment-service");
        provisioningRequest.setTemplateId("template-123");
    }

    @Test
void shouldDeprovisionEnvironmentSuccessfully() {

    DeprovisioningRequest request =
            new DeprovisioningRequest(
                    "env-123",
                    "payment-service-a1234"
            );

    ProvisioningResponse response =
            provisioningService.deprovisionEnvironment(
                    request
            );

    assertEquals(
            "env-123",
            response.getEnvironmentId()
    );

    assertEquals(
            "DELETED",
            response.getStatus()
    );

    assertEquals(
            "Environment cleanup completed",
            response.getMessage()
    );

    verify(environmentCleanupProvisioner)
            .deleteEnvironment(
                    "payment-service-a1234"
            );

    verify(namespaceDeletionChecker)
            .waitUntilDeleted(
                    "devspace-payment-service-a1234"
            );

    ArgumentCaptor<ProvisioningStatusRequest> captor =
            ArgumentCaptor.forClass(
                    ProvisioningStatusRequest.class
            );

    verify(environmentServiceClient)
            .updateProvisioningStatus(
                    eq("env-123"),
                    captor.capture()
            );

    ProvisioningStatusRequest statusRequest =
            captor.getValue();

    assertEquals(
            "DELETED",
            statusRequest.getStatus()
    );
}

    @Test
void shouldMarkEnvironmentFailedWhenDeprovisioningFails() {

    DeprovisioningRequest request =
            new DeprovisioningRequest(
                    "env-123",
                    "payment-service-a1234"
            );

    doThrow(
            new RuntimeException(
                    "Failed to delete namespace"
            )
    ).when(environmentCleanupProvisioner)
            .deleteEnvironment(
                    "payment-service-a1234"
            );

    ProvisioningResponse response =
            provisioningService.deprovisionEnvironment(
                    request
            );

    assertEquals(
            "env-123",
            response.getEnvironmentId()
    );

    assertEquals(
            "FAILED",
            response.getStatus()
    );

    assertEquals(
            "Environment cleanup failed",
            response.getMessage()
    );

    ArgumentCaptor<ProvisioningStatusRequest> captor =
            ArgumentCaptor.forClass(
                    ProvisioningStatusRequest.class
            );

    verify(environmentServiceClient)
            .updateProvisioningStatus(
                    eq("env-123"),
                    captor.capture()
            );

    ProvisioningStatusRequest statusRequest =
            captor.getValue();

    assertEquals(
            "FAILED",
            statusRequest.getStatus()
    );

    assertEquals(
            "Failed to delete namespace",
            statusRequest.getFailureReason()
    );
}

    @Test
void shouldHandleFailureCallbackExceptionGracefully() {

    when(
            provisioningOrchestrator.provisionEnvironment(
                    provisioningRequest
            )
    ).thenThrow(
            new RuntimeException(
                    "Deployment readiness timeout"
            )
    );

    org.mockito.Mockito.doThrow(
            new RuntimeException(
                    "Environment service unavailable"
            )
    ).when(environmentServiceClient)
            .updateProvisioningStatus(
                    eq("env-123"),
                    org.mockito.ArgumentMatchers.any(
                            ProvisioningStatusRequest.class
                    )
            );

    ProvisioningResponse response =
            provisioningService.provisionEnvironment(
                    provisioningRequest
            );

    assertEquals(
            "env-123",
            response.getEnvironmentId()
    );

    assertEquals(
            "FAILED",
            response.getStatus()
    );

    assertEquals(
            "Provisioning failed",
            response.getMessage()
    );
}
}
package com.devspace.provisioning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceTest {

    @Mock
    private ProvisioningOrchestrator provisioningOrchestrator;

    @Mock
    private EnvironmentServiceClient environmentServiceClient;

    @InjectMocks
    private ProvisioningService provisioningService;

    private ProvisioningRequest provisioningRequest;

    @BeforeEach
    void setUp() {

        provisioningRequest = new ProvisioningRequest();

        provisioningRequest.setEnvironmentId("env-123");
        provisioningRequest.setEnvironmentCode("payment-service-a1234");
        provisioningRequest.setApplicationName("payment-service");
        provisioningRequest.setTemplateId("template-123");
    }

    @Test
    void shouldProvisionEnvironmentSuccessfully() {

        String namespace =
                "devspace-payment-service-a1234";

        when(
                provisioningOrchestrator.provisionEnvironment(
                        provisioningRequest
                )
        ).thenReturn(namespace);

        ProvisioningResponse response =
                provisioningService.provisionEnvironment(
                        provisioningRequest
                );

        assertEquals("env-123", response.getEnvironmentId());
        assertEquals("ACCEPTED", response.getStatus());
        assertEquals(
                "Provisioning request accepted",
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

        assertEquals("READY", statusRequest.getStatus());
        assertEquals(
                "devspace-payment-service-a1234",
                statusRequest.getNamespace()
        );
    }

    @Test
    void shouldMarkEnvironmentAsFailedWhenProvisioningFails() {

        when(
                provisioningOrchestrator.provisionEnvironment(
                        provisioningRequest
                )
        ).thenThrow(
                new RuntimeException(
                        "Failed to create namespace"
                )
        );

        ProvisioningResponse response =
                provisioningService.provisionEnvironment(
                        provisioningRequest
                );

        assertEquals("env-123", response.getEnvironmentId());
        assertEquals("FAILED", response.getStatus());
        assertEquals(
                "Provisioning failed",
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

        assertEquals("FAILED", statusRequest.getStatus());
        assertEquals(
                "Failed to create namespace",
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
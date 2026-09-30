package com.devspace.provisioning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.kubernetes.NamespaceProvisioner;
import com.devspace.provisioning.kubernetes.ResourceQuotaProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentProvisioner;
import com.devspace.provisioning.kubernetes.ServiceProvisioner;
import com.devspace.provisioning.kubernetes.IngressProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentReadinessChecker;

@ExtendWith(MockitoExtension.class)
class ProvisioningOrchestratorTest {

    @Mock
    private NamespaceProvisioner namespaceProvisioner;

    @Mock
    private ResourceQuotaProvisioner resourceQuotaProvisioner;

    @Mock
    private DeploymentProvisioner deploymentProvisioner;

    @Mock
    private ServiceProvisioner serviceProvisioner;

    @Mock
    private IngressProvisioner ingressProvisioner;

    @Mock
    private DeploymentReadinessChecker deploymentReadinessChecker;

    @InjectMocks
    private ProvisioningOrchestrator provisioningOrchestrator;

    private ProvisioningRequest provisioningRequest;

    @BeforeEach
    void setUp() {

        provisioningRequest =
                new ProvisioningRequest();

        provisioningRequest.setEnvironmentId(
                "env-123"
        );

        provisioningRequest.setEnvironmentCode(
                "payment-service-a1234"
        );

        provisioningRequest.setApplicationName(
                "payment-service"
        );

        provisioningRequest.setTemplateId(
                "template-123"
        );

        provisioningRequest.setContainerImage(
                "nginx:alpine"
        );

        provisioningRequest.setApplicationPort(
                80
        );

        provisioningRequest.setCpuRequest(
                "100m"
        );

        provisioningRequest.setCpuLimit(
                "500m"
        );

        provisioningRequest.setMemoryRequest(
                "128Mi"
        );

        provisioningRequest.setMemoryLimit(
                "512Mi"
        );
    }

    @Test
    void shouldProvisionEnvironmentSuccessfully() {

        String expectedNamespace =
                "devspace-payment-service-a1234";

        when(
                namespaceProvisioner.createNamespace(
                        provisioningRequest.getEnvironmentCode()
                )
        ).thenReturn(expectedNamespace);

        String actualNamespace =
                provisioningOrchestrator.provisionEnvironment(
                        provisioningRequest
                );

        assertEquals(
                expectedNamespace,
                actualNamespace
        );

        verify(namespaceProvisioner)
                .createNamespace(
                        provisioningRequest.getEnvironmentCode()
                );

        verify(resourceQuotaProvisioner)
                .createResourceQuota(
                        expectedNamespace
                );

        verify(deploymentProvisioner)
                .createDeployment(
                        expectedNamespace,
                        provisioningRequest
                );

        verify(serviceProvisioner)
                .createService(
                        expectedNamespace,
                        provisioningRequest
                );

        verify(ingressProvisioner)
                .createIngress(
                        expectedNamespace,
                        provisioningRequest
                );

        verify(deploymentReadinessChecker)
                .waitUntilReady(
                        expectedNamespace,
                        provisioningRequest.getEnvironmentCode()
                );
    }
}
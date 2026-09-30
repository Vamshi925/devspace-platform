package com.devspace.provisioning.kubernetes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;

import com.devspace.provisioning.dto.request.ProvisioningRequest;

@ExtendWith(MockitoExtension.class)
class DeploymentProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private AppsAPIGroupDSL appsAPIGroupDSL;

    @Mock
    private MixedOperation<
            Deployment,
            DeploymentList,
            RollableScalableResource<Deployment>> deploymentOperations;

    @Mock
    private NonNamespaceOperation<
            Deployment,
            DeploymentList,
            RollableScalableResource<Deployment>> namespacedDeploymentOperations;

    @Mock
    private RollableScalableResource<Deployment> deploymentResource;

    private DeploymentProvisioner deploymentProvisioner;

    private ProvisioningRequest provisioningRequest;

    @BeforeEach
    void setUp() {

        deploymentProvisioner =
                new DeploymentProvisioner(
                        kubernetesClient
                );

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

        when(kubernetesClient.apps())
                .thenReturn(appsAPIGroupDSL);

        when(appsAPIGroupDSL.deployments())
                .thenReturn(deploymentOperations);

        when(
                deploymentOperations.inNamespace(
                        "devspace-payment-service-a1234"
                )
        ).thenReturn(namespacedDeploymentOperations);
    }

    @Test
    void shouldCreateDeploymentWhenDeploymentDoesNotExist() {

        String namespace =
                "devspace-payment-service-a1234";

        String environmentCode =
                provisioningRequest.getEnvironmentCode();

        when(
                namespacedDeploymentOperations.withName(
                        environmentCode
                )
        ).thenReturn(deploymentResource);

        when(deploymentResource.get())
                .thenReturn(null);

        when(
                namespacedDeploymentOperations.resource(
                        any(Deployment.class)
                )
        ).thenReturn(deploymentResource);

        deploymentProvisioner.createDeployment(
                namespace,
                provisioningRequest
        );

        verify(namespacedDeploymentOperations)
                .resource(
                        any(Deployment.class)
                );

        verify(deploymentResource)
                .create();
    }

    @Test
    void shouldNotCreateDuplicateDeployment() {

        String namespace =
                "devspace-payment-service-a1234";

        String environmentCode =
                provisioningRequest.getEnvironmentCode();

        Deployment existingDeployment =
                new Deployment();

        when(
                namespacedDeploymentOperations.withName(
                        environmentCode
                )
        ).thenReturn(deploymentResource);

        when(deploymentResource.get())
                .thenReturn(existingDeployment);

        deploymentProvisioner.createDeployment(
                namespace,
                provisioningRequest
        );

        verify(
                namespacedDeploymentOperations,
                never()
        ).resource(
                any(Deployment.class)
        );

        verify(
                deploymentResource,
                never()
        ).create();
    }
}
package com.devspace.provisioning.kubernetes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;

@ExtendWith(MockitoExtension.class)
class DeploymentReadinessCheckerTest {

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

    private DeploymentReadinessChecker deploymentReadinessChecker;

    private final String namespace =
            "devspace-payment-service-a1234";

    private final String deploymentName =
            "payment-service-a1234";

    @BeforeEach
    void setUp() {

        deploymentReadinessChecker =
                new DeploymentReadinessChecker(
                        kubernetesClient
                );

        when(kubernetesClient.apps())
                .thenReturn(appsAPIGroupDSL);

        when(appsAPIGroupDSL.deployments())
                .thenReturn(deploymentOperations);

        when(
                deploymentOperations.inNamespace(
                        namespace
                )
        ).thenReturn(namespacedDeploymentOperations);

        when(
                namespacedDeploymentOperations.withName(
                        deploymentName
                )
        ).thenReturn(deploymentResource);
    }

    @Test
    void shouldReturnSuccessfullyWhenDeploymentIsReady() {

        Deployment readyDeployment =
                new DeploymentBuilder()
                        .withNewSpec()
                            .withReplicas(1)
                        .endSpec()
                        .withNewStatus()
                            .withAvailableReplicas(1)
                        .endStatus()
                        .build();

        when(
                deploymentResource.waitUntilCondition(
                        any(),
                        anyLong(),
                        eq(TimeUnit.MINUTES)
                )
        ).thenReturn(readyDeployment);

        assertDoesNotThrow(
                () -> deploymentReadinessChecker.waitUntilReady(
                        namespace,
                        deploymentName
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDeploymentDoesNotBecomeReady() {

        Deployment notReadyDeployment =
                new DeploymentBuilder()
                        .withNewSpec()
                            .withReplicas(1)
                        .endSpec()
                        .withNewStatus()
                            .withAvailableReplicas(0)
                        .endStatus()
                        .build();

        when(
                deploymentResource.waitUntilCondition(
                        any(),
                        anyLong(),
                        eq(TimeUnit.MINUTES)
                )
        ).thenReturn(notReadyDeployment);

        assertThrows(
                IllegalStateException.class,
                () -> deploymentReadinessChecker.waitUntilReady(
                        namespace,
                        deploymentName
                )
        );
    }
}
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

import com.devspace.provisioning.dto.request.ProvisioningRequest;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@ExtendWith(MockitoExtension.class)
class ConfigMapProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private MixedOperation<
            ConfigMap,
            ConfigMapList,
            Resource<ConfigMap>> configMapOperations;

    @Mock
    private NonNamespaceOperation<
            ConfigMap,
            ConfigMapList,
            Resource<ConfigMap>> namespacedConfigMapOperations;

    @Mock
    private Resource<ConfigMap> configMapResource;

    private ConfigMapProvisioner configMapProvisioner;

    private ProvisioningRequest provisioningRequest;

    @BeforeEach
    void setUp() {

        configMapProvisioner =
                new ConfigMapProvisioner(
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

        provisioningRequest.setApplicationPort(
                80
        );

        provisioningRequest.setBranchName(
                "main"
        );

        when(kubernetesClient.configMaps())
                .thenReturn(configMapOperations);

        when(
                configMapOperations.inNamespace(
                        "devspace-payment-service-a1234"
                )
        ).thenReturn(namespacedConfigMapOperations);
    }

    @Test
    void shouldCreateConfigMapWhenConfigMapDoesNotExist() {

        String namespace =
                "devspace-payment-service-a1234";

        String configMapName =
                provisioningRequest.getEnvironmentCode()
                        + "-config";

        when(
                namespacedConfigMapOperations.withName(
                        configMapName
                )
        ).thenReturn(configMapResource);

        when(configMapResource.get())
                .thenReturn(null);

        when(
                namespacedConfigMapOperations.resource(
                        any(ConfigMap.class)
                )
        ).thenReturn(configMapResource);

        configMapProvisioner.createConfigMap(
                namespace,
                provisioningRequest
        );

        verify(namespacedConfigMapOperations)
                .resource(
                        any(ConfigMap.class)
                );

        verify(configMapResource)
                .create();
    }

    @Test
    void shouldNotCreateDuplicateConfigMap() {

        String namespace =
                "devspace-payment-service-a1234";

        String configMapName =
                provisioningRequest.getEnvironmentCode()
                        + "-config";

        ConfigMap existingConfigMap =
                new ConfigMap();

        when(
                namespacedConfigMapOperations.withName(
                        configMapName
                )
        ).thenReturn(configMapResource);

        when(configMapResource.get())
                .thenReturn(existingConfigMap);

        configMapProvisioner.createConfigMap(
                namespace,
                provisioningRequest
        );

        verify(
                namespacedConfigMapOperations,
                never()
        ).resource(
                any(ConfigMap.class)
        );

        verify(
                configMapResource,
                never()
        ).create();
    }
}
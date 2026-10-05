package com.devspace.provisioning.kubernetes;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@ExtendWith(MockitoExtension.class)
class EnvironmentCleanupProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private NonNamespaceOperation<
            Namespace,
            NamespaceList,
            Resource<Namespace>> namespaceOperations;

    @Mock
    private Resource<Namespace> namespaceResource;

    private EnvironmentCleanupProvisioner environmentCleanupProvisioner;

    @BeforeEach
    void setUp() {

        environmentCleanupProvisioner =
                new EnvironmentCleanupProvisioner(
                        kubernetesClient
                );

        when(kubernetesClient.namespaces())
                .thenReturn(namespaceOperations);
    }

    @Test
    void shouldDeleteNamespaceWhenNamespaceExists() {

        String environmentCode =
                "payment-service-a1234";

        String namespace =
                "devspace-" + environmentCode;

        Namespace existingNamespace =
                new Namespace();

        when(
                namespaceOperations.withName(namespace)
        ).thenReturn(namespaceResource);

        when(namespaceResource.get())
                .thenReturn(existingNamespace);

        environmentCleanupProvisioner.deleteEnvironment(
                environmentCode
        );

        verify(namespaceResource)
                .delete();
    }

    @Test
    void shouldNotDeleteWhenNamespaceDoesNotExist() {

        String environmentCode =
                "payment-service-a1234";

        String namespace =
                "devspace-" + environmentCode;

        when(
                namespaceOperations.withName(namespace)
        ).thenReturn(namespaceResource);

        when(namespaceResource.get())
                .thenReturn(null);

        environmentCleanupProvisioner.deleteEnvironment(
                environmentCode
        );

        verify(namespaceResource, never())
                .delete();
    }
}
package com.devspace.provisioning.kubernetes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
class NamespaceProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private NonNamespaceOperation<
            Namespace,
            NamespaceList,
            Resource<Namespace>> namespaceOperations;

    @Mock
    private Resource<Namespace> namespaceResource;

    private NamespaceProvisioner namespaceProvisioner;

    @BeforeEach
    void setUp() {

        namespaceProvisioner =
                new NamespaceProvisioner(
                        kubernetesClient
                );

        when(kubernetesClient.namespaces())
                .thenReturn(namespaceOperations);
    }

    @Test
    void shouldCreateNamespaceWhenNamespaceDoesNotExist() {

        String environmentCode =
                "payment-service-a1234";

        String expectedNamespace =
                "devspace-payment-service-a1234";

        when(
                namespaceOperations.withName(
                        expectedNamespace
                )
        ).thenReturn(namespaceResource);

        when(namespaceResource.get())
                .thenReturn(null);

        when(
                namespaceOperations.resource(
                        any(Namespace.class)
                )
        ).thenReturn(namespaceResource);

        String actualNamespace =
                namespaceProvisioner.createNamespace(
                        environmentCode
                );

        assertEquals(
                expectedNamespace,
                actualNamespace
        );

        verify(namespaceOperations)
                .resource(
                        any(Namespace.class)
                );

        verify(namespaceResource)
                .create();
    }

    @Test
    void shouldReturnExistingNamespaceWithoutCreatingAgain() {

        String environmentCode =
                "payment-service-a1234";

        String expectedNamespace =
                "devspace-payment-service-a1234";

        Namespace existingNamespace =
                new Namespace();

        when(
                namespaceOperations.withName(
                        expectedNamespace
                )
        ).thenReturn(namespaceResource);

        when(namespaceResource.get())
                .thenReturn(existingNamespace);

        String actualNamespace =
                namespaceProvisioner.createNamespace(
                        environmentCode
                );

        assertEquals(
                expectedNamespace,
                actualNamespace
        );

        verify(namespaceOperations, never())
                .resource(
                        any(Namespace.class)
                );

        verify(namespaceResource, never())
                .create();
    }
}
package com.devspace.provisioning.kubernetes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
class NamespaceDeletionCheckerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private NonNamespaceOperation<
            Namespace,
            NamespaceList,
            Resource<Namespace>> namespaceOperations;

    @Mock
    private Resource<Namespace> namespaceResource;

    private NamespaceDeletionChecker namespaceDeletionChecker;

    @BeforeEach
    void setUp() {

        namespaceDeletionChecker =
                new NamespaceDeletionChecker(
                        kubernetesClient
                );

        when(kubernetesClient.namespaces())
                .thenReturn(namespaceOperations);

        when(
                namespaceOperations.withName(
                        "devspace-payment-service-a1234"
                )
        ).thenReturn(namespaceResource);
    }

    @Test
    void shouldReturnWhenNamespaceIsAlreadyDeleted() {

        when(namespaceResource.get())
                .thenReturn(null);

        assertDoesNotThrow(
                () -> namespaceDeletionChecker.waitUntilDeleted(
                        "devspace-payment-service-a1234"
                )
        );
    }

    @Test
    void shouldWaitUntilNamespaceIsDeleted() {

        Namespace existingNamespace =
                new Namespace();

        when(namespaceResource.get())
                .thenReturn(existingNamespace)
                .thenReturn(null);

        assertDoesNotThrow(
                () -> namespaceDeletionChecker.waitUntilDeleted(
                        "devspace-payment-service-a1234"
                )
        );
    }
}
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

import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@ExtendWith(MockitoExtension.class)
class ResourceQuotaProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private MixedOperation<
            ResourceQuota,
            ResourceQuotaList,
            Resource<ResourceQuota>> resourceQuotaOperations;

    @Mock
    private NonNamespaceOperation<
            ResourceQuota,
            ResourceQuotaList,
            Resource<ResourceQuota>> namespacedResourceQuotaOperations;

    @Mock
    private Resource<ResourceQuota> resourceQuotaResource;

    private ResourceQuotaProvisioner resourceQuotaProvisioner;

    @BeforeEach
    void setUp() {

        resourceQuotaProvisioner =
                new ResourceQuotaProvisioner(
                        kubernetesClient
                );

        when(kubernetesClient.resourceQuotas())
                .thenReturn(resourceQuotaOperations);

        when(
                resourceQuotaOperations.inNamespace(
                        "devspace-payment-service-a1234"
                )
        ).thenReturn(namespacedResourceQuotaOperations);
    }

    @Test
    void shouldCreateResourceQuotaWhenQuotaDoesNotExist() {

        String namespace =
                "devspace-payment-service-a1234";

        String quotaName =
                "devspace-resource-quota";

        when(
                namespacedResourceQuotaOperations.withName(
                        quotaName
                )
        ).thenReturn(resourceQuotaResource);

        when(resourceQuotaResource.get())
                .thenReturn(null);

        when(
                namespacedResourceQuotaOperations.resource(
                        any(ResourceQuota.class)
                )
        ).thenReturn(resourceQuotaResource);

        resourceQuotaProvisioner.createResourceQuota(
                namespace
        );

        verify(namespacedResourceQuotaOperations)
                .resource(
                        any(ResourceQuota.class)
                );

        verify(resourceQuotaResource)
                .create();
    }

    @Test
    void shouldNotCreateDuplicateResourceQuota() {

        String namespace =
                "devspace-payment-service-a1234";

        String quotaName =
                "devspace-resource-quota";

        ResourceQuota existingQuota =
                new ResourceQuota();

        when(
                namespacedResourceQuotaOperations.withName(
                        quotaName
                )
        ).thenReturn(resourceQuotaResource);

        when(resourceQuotaResource.get())
                .thenReturn(existingQuota);

        resourceQuotaProvisioner.createResourceQuota(
                namespace
        );

        verify(resourceQuotaResource, never())
                .create();
    }
}
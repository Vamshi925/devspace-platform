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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.ServiceResource;

@ExtendWith(MockitoExtension.class)
class ServiceProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private MixedOperation<
            Service,
            ServiceList,
            ServiceResource<Service>> serviceOperations;

    @Mock
    private NonNamespaceOperation<
            Service,
            ServiceList,
            ServiceResource<Service>> namespacedServiceOperations;

    @Mock
    private ServiceResource<Service> serviceResource;

    private ServiceProvisioner serviceProvisioner;

    @BeforeEach
    void setUp() {

        serviceProvisioner =
                new ServiceProvisioner(
                        kubernetesClient
                );

        when(kubernetesClient.services())
                .thenReturn(serviceOperations);

        when(
                serviceOperations.inNamespace(
                        "devspace-payment-service-a1234"
                )
        ).thenReturn(namespacedServiceOperations);
    }

    @Test
    void shouldCreateServiceWhenServiceDoesNotExist() {

        String namespace =
                "devspace-payment-service-a1234";

        String environmentCode =
                "payment-service-a1234";

        when(
                namespacedServiceOperations.withName(
                        environmentCode
                )
        ).thenReturn(serviceResource);

        when(serviceResource.get())
                .thenReturn(null);

        when(
                namespacedServiceOperations.resource(
                        any(Service.class)
                )
        ).thenReturn(serviceResource);

        serviceProvisioner.createService(
                namespace,
                environmentCode
        );

        verify(namespacedServiceOperations)
                .resource(
                        any(Service.class)
                );

        verify(serviceResource)
                .create();
    }

    @Test
    void shouldNotCreateDuplicateService() {

        String namespace =
                "devspace-payment-service-a1234";

        String environmentCode =
                "payment-service-a1234";

        Service existingService =
                new Service();

        when(
                namespacedServiceOperations.withName(
                        environmentCode
                )
        ).thenReturn(serviceResource);

        when(serviceResource.get())
                .thenReturn(existingService);

        serviceProvisioner.createService(
                namespace,
                environmentCode
        );

        verify(namespacedServiceOperations, never())
                .resource(
                        any(Service.class)
                );

        verify(serviceResource, never())
                .create();
    }
}
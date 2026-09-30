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

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.V1NetworkAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NetworkAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@ExtendWith(MockitoExtension.class)
class IngressProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private NetworkAPIGroupDSL networkAPIGroupDSL;

    @Mock
    private V1NetworkAPIGroupDSL v1NetworkAPIGroupDSL;

    @Mock
    private MixedOperation<
            Ingress,
            IngressList,
            Resource<Ingress>> ingressOperations;

    @Mock
    private NonNamespaceOperation<
            Ingress,
            IngressList,
            Resource<Ingress>> namespacedIngressOperations;

    @Mock
    private Resource<Ingress> ingressResource;

    private IngressProvisioner ingressProvisioner;

    @BeforeEach
    void setUp() {

        ingressProvisioner =
                new IngressProvisioner(
                        kubernetesClient
                );

        when(kubernetesClient.network())
                .thenReturn(networkAPIGroupDSL);

        when(networkAPIGroupDSL.v1())
                .thenReturn(v1NetworkAPIGroupDSL);

        when(v1NetworkAPIGroupDSL.ingresses())
                .thenReturn(ingressOperations);

        when(
                ingressOperations.inNamespace(
                        "devspace-payment-service-a1234"
                )
        ).thenReturn(namespacedIngressOperations);
    }

    @Test
    void shouldCreateIngressWhenIngressDoesNotExist() {

        String namespace =
                "devspace-payment-service-a1234";

        String environmentCode =
                "payment-service-a1234";

        String ingressName =
                environmentCode + "-ingress";

        when(
                namespacedIngressOperations.withName(
                        ingressName
                )
        ).thenReturn(ingressResource);

        when(ingressResource.get())
                .thenReturn(null);

        when(
                namespacedIngressOperations.resource(
                        any(Ingress.class)
                )
        ).thenReturn(ingressResource);

        ingressProvisioner.createIngress(
                namespace,
                environmentCode
        );

        verify(namespacedIngressOperations)
                .resource(
                        any(Ingress.class)
                );

        verify(ingressResource)
                .create();
    }

    @Test
    void shouldNotCreateDuplicateIngress() {

        String namespace =
                "devspace-payment-service-a1234";

        String environmentCode =
                "payment-service-a1234";

        String ingressName =
                environmentCode + "-ingress";

        Ingress existingIngress =
                new Ingress();

        when(
                namespacedIngressOperations.withName(
                        ingressName
                )
        ).thenReturn(ingressResource);

        when(ingressResource.get())
                .thenReturn(existingIngress);

        ingressProvisioner.createIngress(
                namespace,
                environmentCode
        );

        verify(namespacedIngressOperations, never())
                .resource(
                        any(Ingress.class)
                );

        verify(ingressResource, never())
                .create();
    }
}
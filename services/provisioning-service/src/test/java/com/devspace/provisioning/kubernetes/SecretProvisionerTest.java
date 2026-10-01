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

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

@ExtendWith(MockitoExtension.class)
class SecretProvisionerTest {

    @Mock
    private KubernetesClient kubernetesClient;

    @Mock
    private MixedOperation<
            Secret,
            SecretList,
            Resource<Secret>> secretOperations;

    @Mock
    private NonNamespaceOperation<
            Secret,
            SecretList,
            Resource<Secret>> namespacedSecretOperations;

    @Mock
    private Resource<Secret> secretResource;

    private SecretProvisioner secretProvisioner;

    private ProvisioningRequest provisioningRequest;

    @BeforeEach
    void setUp() {

        secretProvisioner =
                new SecretProvisioner(
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

        when(kubernetesClient.secrets())
                .thenReturn(secretOperations);

        when(
                secretOperations.inNamespace(
                        "devspace-payment-service-a1234"
                )
        ).thenReturn(namespacedSecretOperations);
    }

    @Test
    void shouldCreateSecretWhenSecretDoesNotExist() {

        String namespace =
                "devspace-payment-service-a1234";

        String secretName =
                provisioningRequest.getEnvironmentCode()
                        + "-secret";

        when(
                namespacedSecretOperations.withName(
                        secretName
                )
        ).thenReturn(secretResource);

        when(secretResource.get())
                .thenReturn(null);

        when(
                namespacedSecretOperations.resource(
                        any(Secret.class)
                )
        ).thenReturn(secretResource);

        secretProvisioner.createSecret(
                namespace,
                provisioningRequest
        );

        verify(namespacedSecretOperations)
                .resource(
                        any(Secret.class)
                );

        verify(secretResource)
                .create();
    }

    @Test
    void shouldNotCreateDuplicateSecret() {

        String namespace =
                "devspace-payment-service-a1234";

        String secretName =
                provisioningRequest.getEnvironmentCode()
                        + "-secret";

        Secret existingSecret =
                new Secret();

        when(
                namespacedSecretOperations.withName(
                        secretName
                )
        ).thenReturn(secretResource);

        when(secretResource.get())
                .thenReturn(existingSecret);

        secretProvisioner.createSecret(
                namespace,
                provisioningRequest
        );

        verify(
                namespacedSecretOperations,
                never()
        ).resource(
                any(Secret.class)
        );

        verify(
                secretResource,
                never()
        ).create();
    }
}
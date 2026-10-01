package com.devspace.provisioning.kubernetes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.devspace.provisioning.dto.request.ProvisioningRequest;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class SecretProvisioner {

    private static final Logger logger =
            LoggerFactory.getLogger(SecretProvisioner.class);

    private final KubernetesClient kubernetesClient;

    public SecretProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createSecret(
            String namespace,
            ProvisioningRequest request) {

        String secretName =
                request.getEnvironmentCode() + "-secret";

        Secret existingSecret =
                kubernetesClient.secrets()
                        .inNamespace(namespace)
                        .withName(secretName)
                        .get();

        if (existingSecret != null) {

            logger.info(
                    "Secret already exists - Namespace: {}, Secret: {}",
                    namespace,
                    secretName
            );

            return;
        }

        Secret secret =
                new SecretBuilder()

                        .withNewMetadata()

                            .withName(
                                    secretName
                            )

                            .withNamespace(
                                    namespace
                            )

                            .addToLabels(
                                    "managed-by",
                                    "devspace"
                            )

                            .addToLabels(
                                    "environment-code",
                                    request.getEnvironmentCode()
                            )

                        .endMetadata()

                        .withType(
                                "Opaque"
                        )

                        .addToData(
                                "DB_USERNAME",
                                encode(
                                        "devspace"
                                )
                        )

                        .addToData(
                                "DB_PASSWORD",
                                encode(
                                        "devspace"
                                )
                        )

                        .build();

        kubernetesClient.secrets()
                .inNamespace(namespace)
                .resource(secret)
                .create();

        logger.info(
                "Secret created successfully - Namespace: {}, Secret: {}",
                namespace,
                secretName
        );
    }

    private String encode(
            String value) {

        return Base64.getEncoder()
                .encodeToString(
                        value.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }
}
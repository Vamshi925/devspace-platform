package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class NamespaceProvisioner {

    private static final Logger logger =
            LoggerFactory.getLogger(NamespaceProvisioner.class);

    private final KubernetesClient kubernetesClient;

    public NamespaceProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public String createNamespace(String environmentCode) {

        String namespaceName =
                "devspace-" + environmentCode;

        Namespace existingNamespace =
                kubernetesClient.namespaces()
                        .withName(namespaceName)
                        .get();

        if (existingNamespace != null) {

            logger.info(
                    "Namespace already exists - Namespace: {}",
                    namespaceName
            );

            return namespaceName;
        }

        Namespace namespace =
                new NamespaceBuilder()
                        .withNewMetadata()
                        .withName(namespaceName)
                        .addToLabels(
                                "managed-by",
                                "devspace"
                        )
                        .addToLabels(
                                "environment-code",
                                environmentCode
                        )
                        .endMetadata()
                        .build();

        kubernetesClient.namespaces()
                .resource(namespace)
                .create();

        logger.info(
                "Namespace created successfully - Namespace: {}",
                namespaceName
        );

        return namespaceName;
    }
}
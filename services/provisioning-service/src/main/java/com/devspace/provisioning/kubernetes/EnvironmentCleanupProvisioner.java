package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class EnvironmentCleanupProvisioner {

    @Value("${devspace.kubernetes.mock:false}")
    private boolean mockKubernetes;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    EnvironmentCleanupProvisioner.class
            );

    private final KubernetesClient kubernetesClient;

    public EnvironmentCleanupProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void deleteEnvironment(
        String environmentCode) {

    String namespace =
            "devspace-" + environmentCode;

    if (mockKubernetes) {

        logger.info(
                "Mock Kubernetes mode - simulating namespace deletion: {}",
                namespace
        );

        return;
    }

    Namespace existingNamespace =
            kubernetesClient.namespaces()
                    .withName(namespace)
                    .get();

    if (existingNamespace == null) {

        logger.info(
                "Namespace already deleted - Namespace: {}",
                namespace
        );

        return;
    }

    kubernetesClient.namespaces()
            .withName(namespace)
            .delete();

    logger.info(
            "Environment cleanup requested - Namespace: {}",
            namespace
    );
}
}
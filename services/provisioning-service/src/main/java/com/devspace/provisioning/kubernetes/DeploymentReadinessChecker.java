package com.devspace.provisioning.kubernetes;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class DeploymentReadinessChecker {

    private static final Logger logger =
            LoggerFactory.getLogger(DeploymentReadinessChecker.class);

    private final KubernetesClient kubernetesClient;

    public DeploymentReadinessChecker(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void waitUntilReady(
            String namespace,
            String deploymentName) {

        logger.info(
                "Waiting for deployment readiness - Namespace: {}, Deployment: {}",
                namespace,
                deploymentName
        );

        Deployment deployment =
                kubernetesClient.apps()
                        .deployments()
                        .inNamespace(namespace)
                        .withName(deploymentName)
                        .waitUntilCondition(
                                this::isDeploymentReady,
                                2,
                                TimeUnit.MINUTES
                        );

        if (deployment == null
                || !isDeploymentReady(deployment)) {

            throw new IllegalStateException(
                    "Deployment did not become ready: "
                            + deploymentName
            );
        }

        logger.info(
                "Deployment is ready - Namespace: {}, Deployment: {}",
                namespace,
                deploymentName
        );
    }

    private boolean isDeploymentReady(
            Deployment deployment) {

        if (deployment == null
                || deployment.getSpec() == null
                || deployment.getStatus() == null) {

            return false;
        }

        Integer desiredReplicas =
                deployment.getSpec().getReplicas();

        Integer availableReplicas =
                deployment.getStatus().getAvailableReplicas();

        if (desiredReplicas == null
                || availableReplicas == null) {

            return false;
        }

        return availableReplicas >= desiredReplicas;
    }
}
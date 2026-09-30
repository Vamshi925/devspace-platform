package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class DeploymentProvisioner {

    private static final Logger logger =
            LoggerFactory.getLogger(DeploymentProvisioner.class);

    private final KubernetesClient kubernetesClient;

    public DeploymentProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createDeployment(
            String namespace,
            String environmentCode) {

        String deploymentName = environmentCode;

        Deployment existingDeployment =
                kubernetesClient.apps()
                        .deployments()
                        .inNamespace(namespace)
                        .withName(deploymentName)
                        .get();

        if (existingDeployment != null) {

            logger.info(
                    "Deployment already exists - Namespace: {}, Deployment: {}",
                    namespace,
                    deploymentName
            );

            return;
        }

        Deployment deployment =
                new DeploymentBuilder()
                        .withNewMetadata()
                            .withName(deploymentName)
                            .withNamespace(namespace)
                            .addToLabels(
                                    "managed-by",
                                    "devspace"
                            )
                            .addToLabels(
                                    "environment-code",
                                    environmentCode
                            )
                            .addToLabels(
                                    "app",
                                    environmentCode
                            )
                        .endMetadata()

                        .withNewSpec()
                            .withReplicas(1)

                            .withNewSelector()
                                .addToMatchLabels(
                                        "app",
                                        environmentCode
                                )
                            .endSelector()

                            .withNewTemplate()

                                .withNewMetadata()
                                    .addToLabels(
                                            "app",
                                            environmentCode
                                    )
                                    .addToLabels(
                                            "managed-by",
                                            "devspace"
                                    )
                                .endMetadata()

                                .withNewSpec()

                                    .addNewContainer()
                                        .withName(environmentCode)

                                        // Temporary image.
                                        // Later this will come from the
                                        // application's built container image.
                                        .withImage("nginx:alpine")

                                        .addNewPort()
                                            .withContainerPort(80)
                                        .endPort()

                                        .withNewResources()

                                            .addToRequests(
                                                    "cpu",
                                                    new Quantity("100m")
                                            )
                                            .addToRequests(
                                                    "memory",
                                                    new Quantity("128Mi")
                                            )

                                            .addToLimits(
                                                    "cpu",
                                                    new Quantity("500m")
                                            )
                                            .addToLimits(
                                                    "memory",
                                                    new Quantity("512Mi")
                                            )

                                        .endResources()

                                    .endContainer()

                                .endSpec()

                            .endTemplate()

                        .endSpec()
                        .build();

        kubernetesClient.apps()
                .deployments()
                .inNamespace(namespace)
                .resource(deployment)
                .create();

        logger.info(
                "Deployment created successfully - Namespace: {}, Deployment: {}",
                namespace,
                deploymentName
        );
    }
}
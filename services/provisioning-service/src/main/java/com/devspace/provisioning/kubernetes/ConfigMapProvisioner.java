package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.devspace.provisioning.dto.request.ProvisioningRequest;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class ConfigMapProvisioner {

    private static final Logger logger =
            LoggerFactory.getLogger(ConfigMapProvisioner.class);

    private final KubernetesClient kubernetesClient;

    public ConfigMapProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createConfigMap(
            String namespace,
            ProvisioningRequest request) {

        String configMapName =
                request.getEnvironmentCode() + "-config";

        ConfigMap existingConfigMap =
                kubernetesClient.configMaps()
                        .inNamespace(namespace)
                        .withName(configMapName)
                        .get();

        if (existingConfigMap != null) {

            logger.info(
                    "ConfigMap already exists - Namespace: {}, ConfigMap: {}",
                    namespace,
                    configMapName
            );

            return;
        }

        ConfigMap configMap =
                new ConfigMapBuilder()

                        .withNewMetadata()

                            .withName(
                                    configMapName
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

                        .addToData(
                                "APPLICATION_NAME",
                                request.getApplicationName()
                        )

                        .addToData(
                                "ENVIRONMENT_CODE",
                                request.getEnvironmentCode()
                        )

                        .addToData(
                                "APPLICATION_PORT",
                                String.valueOf(
                                        request.getApplicationPort()
                                )
                        )

                        .addToData(
                                "BRANCH_NAME",
                                request.getBranchName() != null
                                        ? request.getBranchName()
                                        : "main"
                        )

                        .build();

        kubernetesClient.configMaps()
                .inNamespace(namespace)
                .resource(configMap)
                .create();

        logger.info(
                "ConfigMap created successfully - Namespace: {}, ConfigMap: {}",
                namespace,
                configMapName
        );
    }
}
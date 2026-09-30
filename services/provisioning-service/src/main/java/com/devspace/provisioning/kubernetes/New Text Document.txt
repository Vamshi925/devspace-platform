package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class ServiceProvisioner {

    private static final Logger logger =
            LoggerFactory.getLogger(ServiceProvisioner.class);

    private final KubernetesClient kubernetesClient;

    public ServiceProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createService(
            String namespace,
            String environmentCode) {

        String serviceName = environmentCode;

        Service existingService =
                kubernetesClient.services()
                        .inNamespace(namespace)
                        .withName(serviceName)
                        .get();

        if (existingService != null) {

            logger.info(
                    "Service already exists - Namespace: {}, Service: {}",
                    namespace,
                    serviceName
            );

            return;
        }

        Service service =
                new ServiceBuilder()
                        .withNewMetadata()
                            .withName(serviceName)
                            .withNamespace(namespace)
                            .addToLabels(
                                    "managed-by",
                                    "devspace"
                            )
                            .addToLabels(
                                    "environment-code",
                                    environmentCode
                            )
                        .endMetadata()

                        .withNewSpec()

                            .withType("ClusterIP")

                            .addToSelector(
                                    "app",
                                    environmentCode
                            )

                            .addNewPort()
                                .withName("http")
                                .withProtocol("TCP")
                                .withPort(80)
                                .withTargetPort(
                                        new IntOrString(80)
                                )
                            .endPort()

                        .endSpec()
                        .build();

        kubernetesClient.services()
                .inNamespace(namespace)
                .resource(service)
                .create();

        logger.info(
                "Service created successfully - Namespace: {}, Service: {}",
                namespace,
                serviceName
        );
    }
}
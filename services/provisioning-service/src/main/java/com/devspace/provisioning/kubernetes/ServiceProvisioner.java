package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import com.devspace.provisioning.dto.request.ProvisioningRequest;

@Component
public class ServiceProvisioner {

    @Value("${devspace.kubernetes.mock:false}")
    private boolean mockKubernetes;

    private static final Logger logger =
            LoggerFactory.getLogger(ServiceProvisioner.class);

    private final KubernetesClient kubernetesClient;

    public ServiceProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createService(
            String namespace,
            ProvisioningRequest request) {

        if (mockKubernetes) {

    logger.info(
            "Mock Kubernetes mode - simulating Service creation - Namespace: {}, Service: {}, Port: {}",
            namespace,
            request.getEnvironmentCode(),
            request.getApplicationPort()
    );

    return;
}

        String environmentCode =
                request.getEnvironmentCode();

        String serviceName =
                environmentCode;

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

                                .withPort(
                                        request.getApplicationPort()
                                )

                                .withTargetPort(
                                        new IntOrString(
                                                request.getApplicationPort()
                                        )
                                )

                            .endPort()

                        .endSpec()

                        .build();

        kubernetesClient.services()
                .inNamespace(namespace)
                .resource(service)
                .create();

        logger.info(
                "Service created successfully - Namespace: {}, Service: {}, Port: {}",
                namespace,
                serviceName,
                request.getApplicationPort()
        );
    }
}
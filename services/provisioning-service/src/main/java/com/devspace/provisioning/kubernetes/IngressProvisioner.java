package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import com.devspace.provisioning.dto.request.ProvisioningRequest;

@Component
public class IngressProvisioner {

    private static final Logger logger =
            LoggerFactory.getLogger(IngressProvisioner.class);

    private final KubernetesClient kubernetesClient;

    public IngressProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createIngress(
            String namespace,
            ProvisioningRequest request) {

        String environmentCode =
                request.getEnvironmentCode();

        String ingressName =
                environmentCode + "-ingress";

        String serviceName =
                environmentCode;

        Ingress existingIngress =
                kubernetesClient.network()
                        .v1()
                        .ingresses()
                        .inNamespace(namespace)
                        .withName(ingressName)
                        .get();

        if (existingIngress != null) {

            logger.info(
                    "Ingress already exists - Namespace: {}, Ingress: {}",
                    namespace,
                    ingressName
            );

            return;
        }

        Ingress ingress =
                new IngressBuilder()

                        .withNewMetadata()

                            .withName(ingressName)
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

                            .addNewRule()

                                .withNewHttp()

                                    .addToPaths(

                                            new HTTPIngressPathBuilder()

                                                    .withPath(
                                                            "/" + environmentCode
                                                    )

                                                    .withPathType(
                                                            "Prefix"
                                                    )

                                                    .withNewBackend()

                                                        .withNewService()

                                                            .withName(
                                                                    serviceName
                                                            )

                                                            .withNewPort()

                                                                .withNumber(
                                                                        request.getApplicationPort()
                                                                )

                                                            .endPort()

                                                        .endService()

                                                    .endBackend()

                                                    .build()
                                    )

                                .endHttp()

                            .endRule()

                        .endSpec()

                        .build();

        kubernetesClient.network()
                .v1()
                .ingresses()
                .inNamespace(namespace)
                .resource(ingress)
                .create();

        logger.info(
                "Ingress created successfully - Namespace: {}, Ingress: {}, ServicePort: {}",
                namespace,
                ingressName,
                request.getApplicationPort()
        );
    }
}
package com.devspace.provisioning.kubernetes;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Component
public class ResourceQuotaProvisioner {

    private static final Logger logger =
            LoggerFactory.getLogger(ResourceQuotaProvisioner.class);

    private final KubernetesClient kubernetesClient;

    @Value("${devspace.kubernetes.mock:false}")
    private boolean mockKubernetes;

    public ResourceQuotaProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createResourceQuota(
            String namespace) {

        if (mockKubernetes) {

            logger.info(
                    "Mock Kubernetes mode - simulating ResourceQuota creation - Namespace: {}",
                    namespace
            );

            return;
        }

        String quotaName =
                "devspace-resource-quota";

        ResourceQuota existingQuota =
                kubernetesClient.resourceQuotas()
                        .inNamespace(namespace)
                        .withName(quotaName)
                        .get();

        if (existingQuota != null) {

            logger.info(
                    "ResourceQuota already exists - Namespace: {}, ResourceQuota: {}",
                    namespace,
                    quotaName
            );

            return;
        }

        ResourceQuota resourceQuota =
                new ResourceQuotaBuilder()
                        .withNewMetadata()
                        .withName(quotaName)
                        .withNamespace(namespace)
                        .endMetadata()
                        .withNewSpec()
                        .withHard(
                                Map.of(
                                        "requests.cpu",
                                        new Quantity("2"),

                                        "requests.memory",
                                        new Quantity("4Gi"),

                                        "limits.cpu",
                                        new Quantity("4"),

                                        "limits.memory",
                                        new Quantity("8Gi"),

                                        "pods",
                                        new Quantity("10")
                                )
                        )
                        .endSpec()
                        .build();

        kubernetesClient.resourceQuotas()
                .inNamespace(namespace)
                .resource(resourceQuota)
                .create();

        logger.info(
                "ResourceQuota created successfully - Namespace: {}, ResourceQuota: {}",
                namespace,
                quotaName
        );
    }
}
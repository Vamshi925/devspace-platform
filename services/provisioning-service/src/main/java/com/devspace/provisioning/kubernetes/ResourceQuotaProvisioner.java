package com.devspace.provisioning.kubernetes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ResourceQuotaProvisioner(
            KubernetesClient kubernetesClient) {

        this.kubernetesClient = kubernetesClient;
    }

    public void createResourceQuota(String namespace) {

        String quotaName = "devspace-resource-quota";

        ResourceQuota existingQuota =
                kubernetesClient.resourceQuotas()
                        .inNamespace(namespace)
                        .withName(quotaName)
                        .get();

        if (existingQuota != null) {

            logger.info(
                    "ResourceQuota already exists - Namespace: {}, Quota: {}",
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
                        .addToLabels(
                                "managed-by",
                                "devspace"
                        )
                        .endMetadata()
                        .withNewSpec()
                        .addToHard(
                                "requests.cpu",
                                new Quantity("2")
                        )
                        .addToHard(
                                "requests.memory",
                                new Quantity("4Gi")
                        )
                        .addToHard(
                                "limits.cpu",
                                new Quantity("4")
                        )
                        .addToHard(
                                "limits.memory",
                                new Quantity("8Gi")
                        )
                        .addToHard(
                                "pods",
                                new Quantity("10")
                        )
                        .endSpec()
                        .build();

        kubernetesClient.resourceQuotas()
                .inNamespace(namespace)
                .resource(resourceQuota)
                .create();

        logger.info(
                "ResourceQuota created successfully - Namespace: {}, Quota: {}",
                namespace,
                quotaName
        );
    }
}
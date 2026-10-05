package com.devspace.provisioning.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.kubernetes.NamespaceProvisioner;
import com.devspace.provisioning.kubernetes.ResourceQuotaProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentProvisioner;
import com.devspace.provisioning.kubernetes.ServiceProvisioner;
import com.devspace.provisioning.kubernetes.IngressProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentReadinessChecker;
import com.devspace.provisioning.kubernetes.ConfigMapProvisioner;
import com.devspace.provisioning.kubernetes.SecretProvisioner;

@Service
public class ProvisioningOrchestrator {

    private static final Logger logger =
        LoggerFactory.getLogger(
                ProvisioningOrchestrator.class
        );

    private final NamespaceProvisioner namespaceProvisioner;
    private final ResourceQuotaProvisioner resourceQuotaProvisioner;
    private final DeploymentProvisioner deploymentProvisioner;
    private final ServiceProvisioner serviceProvisioner;
    private final IngressProvisioner ingressProvisioner;
    private final DeploymentReadinessChecker deploymentReadinessChecker;
    private final ConfigMapProvisioner configMapProvisioner;
    private final SecretProvisioner secretProvisioner;

    public ProvisioningOrchestrator(
        NamespaceProvisioner namespaceProvisioner,
        ResourceQuotaProvisioner resourceQuotaProvisioner,
        ConfigMapProvisioner configMapProvisioner,
        SecretProvisioner secretProvisioner,
        DeploymentProvisioner deploymentProvisioner,
        ServiceProvisioner serviceProvisioner,
        IngressProvisioner ingressProvisioner,
        DeploymentReadinessChecker deploymentReadinessChecker) {

    this.namespaceProvisioner =
            namespaceProvisioner;

    this.resourceQuotaProvisioner =
            resourceQuotaProvisioner;

    this.configMapProvisioner =
            configMapProvisioner;

    this.deploymentProvisioner =
            deploymentProvisioner;

    this.secretProvisioner =
            secretProvisioner;

    this.serviceProvisioner =
            serviceProvisioner;

    this.ingressProvisioner =
            ingressProvisioner;

    this.deploymentReadinessChecker =
            deploymentReadinessChecker;
}

    public String provisionEnvironment(
        ProvisioningRequest request) {

    logger.info(
            "Step 1 - Creating namespace"
    );

    String namespace =
            namespaceProvisioner.createNamespace(
                    request.getEnvironmentCode()
            );

    logger.info(
            "Step 2 - Creating ResourceQuota"
    );

    resourceQuotaProvisioner.createResourceQuota(
            namespace
    );

    logger.info(
            "Step 3 - Creating ConfigMap"
    );

    configMapProvisioner.createConfigMap(
            namespace,
            request
    );

    logger.info(
            "Step 4 - Creating Secret"
    );

    secretProvisioner.createSecret(
            namespace,
            request
    );

    logger.info(
            "Step 5 - Creating Deployment"
    );

    deploymentProvisioner.createDeployment(
            namespace,
            request
    );

    logger.info(
            "Step 6 - Creating Service"
    );

    serviceProvisioner.createService(
            namespace,
            request
    );

    logger.info(
            "Step 7 - Creating Ingress"
    );

    ingressProvisioner.createIngress(
            namespace,
            request
    );

    logger.info(
            "Step 8 - Waiting for Deployment readiness"
    );

    deploymentReadinessChecker.waitUntilReady(
            namespace,
            request.getEnvironmentCode()
    );

    logger.info(
            "Provisioning completed successfully - EnvironmentId: {}",
            request.getEnvironmentId()
    );

    return namespace;
}
}
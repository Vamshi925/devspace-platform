package com.devspace.provisioning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.kubernetes.ConfigMapProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentReadinessChecker;
import com.devspace.provisioning.kubernetes.IngressProvisioner;
import com.devspace.provisioning.kubernetes.NamespaceProvisioner;
import com.devspace.provisioning.kubernetes.ResourceQuotaProvisioner;
import com.devspace.provisioning.kubernetes.SecretProvisioner;
import com.devspace.provisioning.kubernetes.ServiceProvisioner;

@Service
public class ProvisioningOrchestrator {

    private static final Logger logger =
            LoggerFactory.getLogger(ProvisioningOrchestrator.class);

    private final NamespaceProvisioner namespaceProvisioner;
    private final ResourceQuotaProvisioner resourceQuotaProvisioner;
    private final ConfigMapProvisioner configMapProvisioner;
    private final SecretProvisioner secretProvisioner;
    private final DeploymentProvisioner deploymentProvisioner;
    private final ServiceProvisioner serviceProvisioner;
    private final IngressProvisioner ingressProvisioner;
    private final DeploymentReadinessChecker readinessChecker;
    private final ProvisioningProgressReporter progressReporter;

    public ProvisioningOrchestrator(
            NamespaceProvisioner namespaceProvisioner,
            ResourceQuotaProvisioner resourceQuotaProvisioner,
            ConfigMapProvisioner configMapProvisioner,
            SecretProvisioner secretProvisioner,
            DeploymentProvisioner deploymentProvisioner,
            ServiceProvisioner serviceProvisioner,
            IngressProvisioner ingressProvisioner,
            DeploymentReadinessChecker readinessChecker,
            ProvisioningProgressReporter progressReporter) {

        this.namespaceProvisioner = namespaceProvisioner;
        this.resourceQuotaProvisioner = resourceQuotaProvisioner;
        this.configMapProvisioner = configMapProvisioner;
        this.secretProvisioner = secretProvisioner;
        this.deploymentProvisioner = deploymentProvisioner;
        this.serviceProvisioner = serviceProvisioner;
        this.ingressProvisioner = ingressProvisioner;
        this.readinessChecker = readinessChecker;
        this.progressReporter = progressReporter;
    }

    public String provisionEnvironment(ProvisioningRequest request) {

        String environmentId = request.getEnvironmentId();

        logger.info("Step 1 - Creating namespace");
        String namespace =
                namespaceProvisioner.createNamespace(
                        request.getEnvironmentCode()
                );

        report(environmentId, "NAMESPACE_CREATED");

        logger.info("Step 2 - Configuring environment resources");

        resourceQuotaProvisioner.createResourceQuota(namespace);
        configMapProvisioner.createConfigMap(namespace, request);
        secretProvisioner.createSecret(namespace, request);

        report(environmentId, "RESOURCES_CONFIGURED");

        logger.info("Step 3 - Creating Deployment");
        deploymentProvisioner.createDeployment(namespace, request);

        report(environmentId, "DEPLOYMENT_CREATED");

        logger.info("Step 4 - Creating Service");
        serviceProvisioner.createService(namespace, request);

        report(environmentId, "SERVICE_CREATED");

        logger.info("Step 5 - Creating Ingress");
        ingressProvisioner.createIngress(namespace, request);

        report(environmentId, "INGRESS_CREATED");

        logger.info("Step 6 - Waiting for Deployment readiness");

        report(environmentId, "WAITING_FOR_READINESS");

        readinessChecker.waitUntilReady(
                namespace,
                request.getEnvironmentCode()
        );

        logger.info(
                "Provisioning completed successfully - EnvironmentId: {}",
                environmentId
        );

        return namespace;
    }

    private void report(String environmentId, String stage) {
        progressReporter.report(environmentId, stage);
    }
}
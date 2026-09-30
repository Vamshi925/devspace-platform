package com.devspace.provisioning.service;

import org.springframework.stereotype.Service;

import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.kubernetes.NamespaceProvisioner;
import com.devspace.provisioning.kubernetes.ResourceQuotaProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentProvisioner;
import com.devspace.provisioning.kubernetes.ServiceProvisioner;
import com.devspace.provisioning.kubernetes.IngressProvisioner;
import com.devspace.provisioning.kubernetes.DeploymentReadinessChecker;

@Service
public class ProvisioningOrchestrator {

    private final NamespaceProvisioner namespaceProvisioner;
    private final ResourceQuotaProvisioner resourceQuotaProvisioner;
    private final DeploymentProvisioner deploymentProvisioner;
    private final ServiceProvisioner serviceProvisioner;
    private final IngressProvisioner ingressProvisioner;
    private final DeploymentReadinessChecker deploymentReadinessChecker;

    public ProvisioningOrchestrator(
            NamespaceProvisioner namespaceProvisioner,
            ResourceQuotaProvisioner resourceQuotaProvisioner,
            DeploymentProvisioner deploymentProvisioner,
            ServiceProvisioner serviceProvisioner,
            IngressProvisioner ingressProvisioner,
            DeploymentReadinessChecker deploymentReadinessChecker) {

        this.namespaceProvisioner =
                namespaceProvisioner;

        this.resourceQuotaProvisioner =
                resourceQuotaProvisioner;

        this.deploymentProvisioner =
                deploymentProvisioner;

        this.serviceProvisioner =
                serviceProvisioner;

        this.ingressProvisioner =
                ingressProvisioner;

        this.deploymentReadinessChecker =
                deploymentReadinessChecker;
    }

    public String provisionEnvironment(
            ProvisioningRequest request) {

        String namespace =
                namespaceProvisioner.createNamespace(
                        request.getEnvironmentCode()
                );

        resourceQuotaProvisioner.createResourceQuota(
                namespace
        );

        deploymentProvisioner.createDeployment(
                namespace,
                request
        );

        serviceProvisioner.createService(
                namespace,
                request
        );

        ingressProvisioner.createIngress(
                namespace,
                request
        );

        deploymentReadinessChecker.waitUntilReady(
                namespace,
                request.getEnvironmentCode()
        );

        return namespace;
    }
}
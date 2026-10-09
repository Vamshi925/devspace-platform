package com.devspace.environment.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devspace.environment.client.GitHubRepositoryClient;
import com.devspace.environment.client.NotificationServiceClient;
import com.devspace.environment.client.ProvisioningServiceClient;
import com.devspace.environment.client.TemplateServiceClient;
import com.devspace.environment.dto.request.CreateEnvironmentRequest;
import com.devspace.environment.dto.request.DeprovisioningRequest;
import com.devspace.environment.dto.request.ProvisioningRequest;
import com.devspace.environment.dto.request.ProvisioningStatusRequest;
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.dto.response.ProvisioningResponse;
import com.devspace.environment.dto.response.TemplateResponse;
import com.devspace.environment.exception.EnvironmentAccessDeniedException;
import com.devspace.environment.exception.EnvironmentNotFoundException;
import com.devspace.environment.exception.ProvisioningServiceUnavailableException;
import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.repository.EnvironmentRepository;

@Service
public class EnvironmentService {

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private TemplateServiceClient templateServiceClient;

    @Autowired
    private ProvisioningServiceClient provisioningServiceClient;

    @Autowired
    private GitHubRepositoryClient gitHubRepositoryClient;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    public EnvironmentResponse createEnvironment(
            CreateEnvironmentRequest request,
            String userId) {

        validateLifetime(request.getLifetimeHours());

        TemplateResponse template =
                templateServiceClient.getTemplateById(
                        request.getTemplateId()
                );

        if (template == null
                || Boolean.FALSE.equals(template.getActive())) {
            throw new IllegalArgumentException(
                    "Selected template is not available"
            );
        }

        gitHubRepositoryClient.validateRepositoryAndBranch(
                request.getRepositoryUrl(),
                request.getBranchName()
        );

        Environment environment = convertToEntity(request);
        environment.setUserId(userId);
        environment.setEnvironmentCode(
                generateEnvironmentCode(
                        request.getApplicationName()
                )
        );
        environment.setStatus(EnvironmentStatus.REQUESTED);
        environment.setExpiresAt(
                Instant.now().plus(
                        request.getLifetimeHours(),
                        ChronoUnit.HOURS
                )
        );

        Environment savedEnvironment =
                environmentRepository.save(environment);

        savedEnvironment.setStatus(
                EnvironmentStatus.PROVISIONING
        );
        savedEnvironment.setFailureReason(null);

        savedEnvironment =
                environmentRepository.save(savedEnvironment);

        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(
                        savedEnvironment.getEnvironmentId(),
                        savedEnvironment.getEnvironmentCode(),
                        savedEnvironment.getApplicationName(),
                        savedEnvironment.getTemplateId(),
                        savedEnvironment.getExpiresAt(),
                        savedEnvironment.getRepositoryUrl(),
                        savedEnvironment.getBranchName(),
                        template.getContainerImage(),
                        template.getApplicationPort(),
                        template.getCpuRequest(),
                        template.getCpuLimit(),
                        template.getMemoryRequest(),
                        template.getMemoryLimit()
                );

        try {
            ProvisioningResponse response =
                    provisioningServiceClient
                            .provisionEnvironment(
                                    provisioningRequest
                            );

            if (response == null) {
                savedEnvironment.setStatus(
                        EnvironmentStatus.FAILED
                );
                savedEnvironment.setFailureReason(
                        "Provisioning Service returned an empty response"
                );

                savedEnvironment =
                        environmentRepository.save(
                                savedEnvironment
                        );

                sendFailedNotification(savedEnvironment);

            } else if ("FAILED".equalsIgnoreCase(
                    response.getStatus())) {

                savedEnvironment.setStatus(
                        EnvironmentStatus.FAILED
                );
                savedEnvironment.setFailureReason(
                        response.getMessage()
                );

                savedEnvironment =
                        environmentRepository.save(
                                savedEnvironment
                        );

                sendFailedNotification(savedEnvironment);

            } else if (!"ACCEPTED".equalsIgnoreCase(
                    response.getStatus())) {

                savedEnvironment.setStatus(
                        EnvironmentStatus.FAILED
                );
                savedEnvironment.setFailureReason(
                        "Provisioning Service did not accept "
                                + "the provisioning request"
                );

                savedEnvironment =
                        environmentRepository.save(
                                savedEnvironment
                        );

                sendFailedNotification(savedEnvironment);
            }

        } catch (ProvisioningServiceUnavailableException ex) {
            savedEnvironment.setStatus(
                    EnvironmentStatus.FAILED
            );
            savedEnvironment.setFailureReason(
                    ex.getMessage()
            );

            savedEnvironment =
                    environmentRepository.save(
                            savedEnvironment
                    );

            sendFailedNotification(savedEnvironment);
        }

        Environment latestEnvironment =
                environmentRepository
                        .findById(
                                savedEnvironment
                                        .getEnvironmentId()
                        )
                        .orElse(savedEnvironment);

        return convertToDTO(latestEnvironment);
    }

    public EnvironmentResponse getEnvironmentById(
            String environmentId,
            String userId,
            String role) {

        Environment environment =
                getEnvironment(environmentId);

        validateEnvironmentAccess(
                environment,
                userId,
                role
        );

        return convertToDTO(environment);
    }

    public List<EnvironmentResponse> getAllEnvironments() {
        return environmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnvironmentResponse> getEnvironmentsByUserId(
            String userId) {

        return environmentRepository
                .findEnvironmentsByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EnvironmentResponse extendEnvironment(
            String environmentId,
            String userId,
            String role,
            Integer additionalHours) {

        validateLifetime(additionalHours);

        Environment environment =
                getEnvironment(environmentId);

        validateEnvironmentAccess(
                environment,
                userId,
                role
        );

        if (environment.getStatus()
                != EnvironmentStatus.READY) {

            throw new IllegalArgumentException(
                    "Environment can only be extended "
                            + "while in READY status"
            );
        }

        environment.setExpiresAt(
                environment.getExpiresAt()
                        .plus(
                                additionalHours,
                                ChronoUnit.HOURS
                        )
        );

        return convertToDTO(
                environmentRepository.save(environment)
        );
    }

    public EnvironmentResponse deleteEnvironment(
            String environmentId,
            String userId,
            String role) {

        Environment environment =
                getEnvironment(environmentId);

        validateEnvironmentAccess(
                environment,
                userId,
                role
        );

        if (environment.getStatus()
                == EnvironmentStatus.DELETED) {

            throw new IllegalArgumentException(
                    "Environment is already deleted"
            );
        }

        if (environment.getStatus()
                == EnvironmentStatus.DELETING) {

            throw new IllegalArgumentException(
                    "Environment deletion is already in progress"
            );
        }

        if (environment.getStatus()
                == EnvironmentStatus.PROVISIONING) {

            throw new IllegalArgumentException(
                    "Environment cannot be deleted while "
                            + "provisioning is in progress"
            );
        }

        environment.setStatus(
                EnvironmentStatus.DELETING
        );
        environment.setFailureReason(null);

        environment =
                environmentRepository.save(environment);

        DeprovisioningRequest request =
                new DeprovisioningRequest(
                        environment.getEnvironmentId(),
                        environment.getEnvironmentCode()
                );

        try {
            ProvisioningResponse response =
                    provisioningServiceClient
                            .deprovisionEnvironment(request);

            if (response == null) {
                environment.setFailureReason(
                        "Provisioning Service returned "
                                + "an empty response during cleanup"
                );
                environmentRepository.save(environment);

            } else if ("FAILED".equalsIgnoreCase(
                    response.getStatus())) {

                environment.setFailureReason(
                        response.getMessage()
                );
                environmentRepository.save(environment);
            }

        } catch (ProvisioningServiceUnavailableException ex) {
            environment.setFailureReason(ex.getMessage());
            environmentRepository.save(environment);
            throw ex;
        }

        Environment latestEnvironment =
                environmentRepository
                        .findById(environmentId)
                        .orElse(environment);

        return convertToDTO(latestEnvironment);
    }

    public void expireEnvironment(String environmentId) {

        Environment environment =
                getEnvironment(environmentId);

        if (environment.getStatus()
                != EnvironmentStatus.READY
                && environment.getStatus()
                != EnvironmentStatus.EXPIRED) {
            return;
        }

        if (environment.getExpiresAt()
                .isAfter(Instant.now())) {
            return;
        }

        if (environment.getStatus()
                == EnvironmentStatus.READY) {

            environment.setStatus(
                    EnvironmentStatus.EXPIRED
            );
            environment.setFailureReason(null);

            environment =
                    environmentRepository.save(environment);
        }

        environment.setStatus(
                EnvironmentStatus.DELETING
        );
        environment.setFailureReason(null);

        environment =
                environmentRepository.save(environment);

        DeprovisioningRequest request =
                new DeprovisioningRequest(
                        environment.getEnvironmentId(),
                        environment.getEnvironmentCode()
                );

        try {
            ProvisioningResponse response =
                    provisioningServiceClient
                            .deprovisionEnvironment(request);

            if (response == null) {
                markExpirationCleanupFailed(
                        environment,
                        "Provisioning Service returned "
                                + "an empty response during "
                                + "expiration cleanup"
                );

            } else if ("FAILED".equalsIgnoreCase(
                    response.getStatus())) {

                markExpirationCleanupFailed(
                        environment,
                        response.getMessage()
                );
            }

        } catch (ProvisioningServiceUnavailableException ex) {
            markExpirationCleanupFailed(
                    environment,
                    ex.getMessage()
            );
        }
    }

    public EnvironmentResponse updateProvisioningStatus(
            String environmentId,
            ProvisioningStatusRequest request) {

        Environment environment =
                getEnvironment(environmentId);

        String status = request.getStatus();

        if ("READY".equalsIgnoreCase(status)) {

            if (environment.getStatus()
                    != EnvironmentStatus.PROVISIONING) {

                throw new IllegalArgumentException(
                        "Environment must be in PROVISIONING "
                                + "status before becoming READY"
                );
            }

            environment.setStatus(
                    EnvironmentStatus.READY
            );
            environment.setNamespace(
                    request.getNamespace()
            );
            environment.setApplicationUrl(
                    request.getApplicationUrl()
            );
            environment.setFailureReason(null);

            environment =
                    environmentRepository.save(environment);

            sendNotification(
                    environment,
                    "ENVIRONMENT_READY",
                    "Environment Ready",
                    "Your environment "
                            + environment.getApplicationName()
                            + " is ready."
            );

        } else if ("FAILED".equalsIgnoreCase(status)) {

            if (environment.getStatus()
                    != EnvironmentStatus.PROVISIONING
                    && environment.getStatus()
                    != EnvironmentStatus.DELETING
                    && environment.getStatus()
                    != EnvironmentStatus.EXPIRED) {

                throw new IllegalArgumentException(
                        "Environment cannot be marked FAILED "
                                + "from status: "
                                + environment.getStatus()
                );
            }

            environment.setStatus(
                    EnvironmentStatus.FAILED
            );
            environment.setFailureReason(
                    request.getFailureReason()
            );

            environment =
                    environmentRepository.save(environment);

            sendFailedNotification(environment);

        } else if ("DELETED".equalsIgnoreCase(status)) {

            if (environment.getStatus()
                    != EnvironmentStatus.DELETING
                    && environment.getStatus()
                    != EnvironmentStatus.EXPIRED) {

                throw new IllegalArgumentException(
                        "Environment cannot be marked DELETED "
                                + "from status: "
                                + environment.getStatus()
                );
            }

            environment.setStatus(
                    EnvironmentStatus.DELETED
            );
            environment.setFailureReason(null);

            environment =
                    environmentRepository.save(environment);

            sendNotification(
                    environment,
                    "ENVIRONMENT_DELETED",
                    "Environment Deleted",
                    "Environment "
                            + environment.getApplicationName()
                            + " has been deleted."
            );

        } else {
            throw new IllegalArgumentException(
                    "Unsupported provisioning status: "
                            + status
            );
        }

        return convertToDTO(environment);
    }

    private Environment getEnvironment(
            String environmentId) {

        return environmentRepository
                .findById(environmentId)
                .orElseThrow(() ->
                        new EnvironmentNotFoundException(
                                "Environment not found with id: "
                                        + environmentId
                        )
                );
    }

    private void validateEnvironmentAccess(
            Environment environment,
            String userId,
            String role) {

        if ("ROLE_ADMIN".equals(role)) {
            return;
        }

        if (!environment.getUserId().equals(userId)) {
            throw new EnvironmentAccessDeniedException(
                    "You are not allowed to access "
                            + "this environment"
            );
        }
    }

    private void markExpirationCleanupFailed(
            Environment environment,
            String failureReason) {

        environment.setStatus(
                EnvironmentStatus.EXPIRED
        );
        environment.setFailureReason(failureReason);

        environmentRepository.save(environment);
    }

    private void validateLifetime(
            Integer lifetimeHours) {

        if (!List.of(2, 4, 8, 24)
                .contains(lifetimeHours)) {

            throw new IllegalArgumentException(
                    "Lifetime must be one of: "
                            + "2, 4, 8, 24 hours"
            );
        }
    }

    private String generateEnvironmentCode(
            String applicationName) {

        return applicationName
                .toLowerCase()
                .replace(" ", "-")
                + "-"
                + UUID.randomUUID()
                        .toString()
                        .substring(0, 5);
    }

    private void sendFailedNotification(
            Environment environment) {

        sendNotification(
                environment,
                "ENVIRONMENT_FAILED",
                "Environment Failed",
                "Environment "
                        + environment.getApplicationName()
                        + " failed."
        );
    }

    private void sendNotification(
            Environment environment,
            String type,
            String title,
            String message) {

        try {
            notificationServiceClient.sendNotification(
                    new NotificationServiceClient
                            .NotificationRequest(
                                    environment.getUserId(),
                                    environment.getEnvironmentId(),
                                    type,
                                    title,
                                    message
                            )
            );

        } catch (Exception ex) {
            System.out.println(
                    "Notification Service unavailable "
                            + "for environment: "
                            + environment.getEnvironmentId()
            );
        }
    }

  public void checkExpiringEnvironment(String environmentId) {
    Environment env = getEnvironment(environmentId);

    if (env.getStatus() != EnvironmentStatus.READY
            || env.isExpirationNotificationSent()) {
        return;
    }

    Instant now = Instant.now();

    if (env.getExpiresAt().isAfter(now)
            && !env.getExpiresAt()
                    .isAfter(now.plus(30, ChronoUnit.MINUTES))) {

        sendNotification(
                env,
                "ENVIRONMENT_EXPIRING",
                "Environment Expiring Soon",
                "Environment " + env.getApplicationName()
                        + " will expire within 30 minutes."
        );

        env.setExpirationNotificationSent(true);
        environmentRepository.save(env);
    }
}

    private Environment convertToEntity(
            CreateEnvironmentRequest request) {

        Environment environment =
                new Environment();

        environment.setApplicationName(
                request.getApplicationName()
        );
        environment.setTemplateId(
                request.getTemplateId()
        );
        environment.setEnvironmentType(
                request.getEnvironmentType()
        );
        environment.setRepositoryUrl(
                request.getRepositoryUrl()
        );
        environment.setBranchName(
                request.getBranchName()
        );

        return environment;
    }

    private EnvironmentResponse convertToDTO(
            Environment environment) {

        return new EnvironmentResponse(
                environment.getEnvironmentId(),
                environment.getEnvironmentCode(),
                environment.getApplicationName(),
                environment.getUserId(),
                environment.getTemplateId(),
                environment.getEnvironmentType(),
                environment.getStatus(),
                environment.getCreatedAt(),
                environment.getUpdatedAt(),
                environment.getExpiresAt(),
                environment.getNamespace(),
                environment.getApplicationUrl(),
                environment.getRepositoryUrl(),
                environment.getBranchName(),
                environment.getFailureReason()
        );
    }
}
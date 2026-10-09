package com.devspace.environment.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

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
import com.devspace.environment.dto.response.EnvironmentActivityResponse;
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.dto.response.ProvisioningResponse;
import com.devspace.environment.dto.response.TemplateResponse;
import com.devspace.environment.dto.response.DashboardSummaryResponse;
import com.devspace.environment.exception.EnvironmentAccessDeniedException;
import com.devspace.environment.exception.EnvironmentNotFoundException;
import com.devspace.environment.exception.ProvisioningServiceUnavailableException;
import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentActivity;
import com.devspace.environment.model.EnvironmentActivityType;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.repository.EnvironmentActivityRepository;
import com.devspace.environment.repository.EnvironmentRepository;

@Service
public class EnvironmentService {

    @Autowired
    private EnvironmentRepository environmentRepository;
    @Autowired
    private EnvironmentActivityRepository activityRepository;
    @Autowired
    private TemplateServiceClient templateServiceClient;
    @Autowired
    private ProvisioningServiceClient provisioningServiceClient;
    @Autowired
    private GitHubRepositoryClient gitHubRepositoryClient;
    @Autowired
    private NotificationServiceClient notificationServiceClient;

    public EnvironmentResponse createEnvironment(
            CreateEnvironmentRequest request, String userId) {

        validateLifetime(request.getLifetimeHours());

        TemplateResponse template =
                templateServiceClient.getTemplateById(request.getTemplateId());

        if (template == null || Boolean.FALSE.equals(template.getActive())) {
            throw new IllegalArgumentException(
                    "Selected template is not available"
            );
        }

        gitHubRepositoryClient.validateRepositoryAndBranch(
                request.getRepositoryUrl(),
                request.getBranchName()
        );

        Environment env = convertToEntity(request);
        env.setUserId(userId);
        env.setEnvironmentCode(generateEnvironmentCode(request.getApplicationName()));
        env.setStatus(EnvironmentStatus.REQUESTED);
        env.setExpiresAt(
                Instant.now().plus(request.getLifetimeHours(), ChronoUnit.HOURS)
        );

        env = environmentRepository.save(env);
        recordActivity(env, EnvironmentActivityType.CREATED, "Environment created");

        env.setStatus(EnvironmentStatus.PROVISIONING);
        env.setFailureReason(null);
        env = environmentRepository.save(env);

        recordActivity(
                env,
                EnvironmentActivityType.PROVISIONING_STARTED,
                "Environment provisioning started"
        );

        ProvisioningRequest provisioningRequest = new ProvisioningRequest(
                env.getEnvironmentId(),
                env.getEnvironmentCode(),
                env.getApplicationName(),
                env.getTemplateId(),
                env.getExpiresAt(),
                env.getRepositoryUrl(),
                env.getBranchName(),
                template.getContainerImage(),
                template.getApplicationPort(),
                template.getCpuRequest(),
                template.getCpuLimit(),
                template.getMemoryRequest(),
                template.getMemoryLimit()
        );

        try {
            ProvisioningResponse response =
                    provisioningServiceClient.provisionEnvironment(provisioningRequest);

            if (response == null) {
                env = markFailed(
                        env,
                        "Provisioning Service returned an empty response"
                );
            } else if ("FAILED".equalsIgnoreCase(response.getStatus())) {
                env = markFailed(env, response.getMessage());
            } else if (!"ACCEPTED".equalsIgnoreCase(response.getStatus())) {
                env = markFailed(
                        env,
                        "Provisioning Service did not accept the provisioning request"
                );
            }
        } catch (ProvisioningServiceUnavailableException ex) {
            env = markFailed(env, ex.getMessage());
        }

        Environment latest = environmentRepository
                .findById(env.getEnvironmentId())
                .orElse(env);

        return convertToDTO(latest);
    }

    public EnvironmentResponse getEnvironmentById(
            String environmentId, String userId, String role) {

        Environment env = getEnvironment(environmentId);
        validateEnvironmentAccess(env, userId, role);
        return convertToDTO(env);
    }

    public List<EnvironmentResponse> getAllEnvironments() {
        return environmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<EnvironmentResponse> getEnvironmentsByUserId(String userId) {
        return environmentRepository
                .findEnvironmentsByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public EnvironmentResponse extendEnvironment(
            String environmentId,
            String userId,
            String role,
            Integer additionalHours) {

        validateLifetime(additionalHours);

        Environment env = getEnvironment(environmentId);
        validateEnvironmentAccess(env, userId, role);

        if (env.getStatus() != EnvironmentStatus.READY) {
            throw new IllegalArgumentException(
                    "Environment can only be extended while in READY status"
            );
        }

        env.setExpiresAt(
                env.getExpiresAt().plus(additionalHours, ChronoUnit.HOURS)
        );
        env.setExpirationNotificationSent(false);

        env = environmentRepository.save(env);

        recordActivity(
                env,
                EnvironmentActivityType.EXTENDED,
                "Environment extended by " + additionalHours + " hours"
        );

        return convertToDTO(env);
    }

    public EnvironmentResponse deleteEnvironment(
            String environmentId, String userId, String role) {

        Environment env = getEnvironment(environmentId);
        validateEnvironmentAccess(env, userId, role);

        if (env.getStatus() == EnvironmentStatus.DELETED) {
            throw new IllegalArgumentException("Environment is already deleted");
        }

        if (env.getStatus() == EnvironmentStatus.DELETING) {
            throw new IllegalArgumentException(
                    "Environment deletion is already in progress"
            );
        }

        if (env.getStatus() == EnvironmentStatus.PROVISIONING) {
            throw new IllegalArgumentException(
                    "Environment cannot be deleted while provisioning is in progress"
            );
        }

        env.setStatus(EnvironmentStatus.DELETING);
        env.setFailureReason(null);
        env = environmentRepository.save(env);

        recordActivity(
                env,
                EnvironmentActivityType.DELETION_REQUESTED,
                "Environment deletion requested"
        );

        DeprovisioningRequest request =
                new DeprovisioningRequest(
                        env.getEnvironmentId(),
                        env.getEnvironmentCode()
                );

        try {
            ProvisioningResponse response =
                    provisioningServiceClient.deprovisionEnvironment(request);

            if (response == null) {
                env.setFailureReason(
                        "Provisioning Service returned an empty response during cleanup"
                );
                environmentRepository.save(env);
            } else if ("FAILED".equalsIgnoreCase(response.getStatus())) {
                env.setFailureReason(response.getMessage());
                environmentRepository.save(env);
            }
        } catch (ProvisioningServiceUnavailableException ex) {
            env.setFailureReason(ex.getMessage());
            environmentRepository.save(env);
            throw ex;
        }

        Environment latest =
                environmentRepository.findById(environmentId).orElse(env);

        return convertToDTO(latest);
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

            recordActivity(
                    env,
                    EnvironmentActivityType.EXPIRING,
                    "Environment is expiring within 30 minutes"
            );
        }
    }

    public void expireEnvironment(String environmentId) {
        Environment env = getEnvironment(environmentId);

        if (env.getStatus() != EnvironmentStatus.READY
                && env.getStatus() != EnvironmentStatus.EXPIRED) {
            return;
        }

        if (env.getExpiresAt().isAfter(Instant.now())) {
            return;
        }

        if (env.getStatus() == EnvironmentStatus.READY) {
            env.setStatus(EnvironmentStatus.EXPIRED);
            env.setFailureReason(null);
            env = environmentRepository.save(env);

            sendNotification(
                    env,
                    "ENVIRONMENT_EXPIRED",
                    "Environment Expired",
                    "Environment " + env.getApplicationName() + " has expired."
            );

            recordActivity(
                    env,
                    EnvironmentActivityType.EXPIRED,
                    "Environment expired"
            );
        }

        env.setStatus(EnvironmentStatus.DELETING);
        env.setFailureReason(null);
        env = environmentRepository.save(env);

        DeprovisioningRequest request =
                new DeprovisioningRequest(
                        env.getEnvironmentId(),
                        env.getEnvironmentCode()
                );

        try {
            ProvisioningResponse response =
                    provisioningServiceClient.deprovisionEnvironment(request);

            if (response == null) {
                markExpirationCleanupFailed(
                        env,
                        "Provisioning Service returned an empty response during expiration cleanup"
                );
            } else if ("FAILED".equalsIgnoreCase(response.getStatus())) {
                markExpirationCleanupFailed(env, response.getMessage());
            }
        } catch (ProvisioningServiceUnavailableException ex) {
            markExpirationCleanupFailed(env, ex.getMessage());
        }
    }

    public EnvironmentResponse updateProvisioningStatus(
            String environmentId,
            ProvisioningStatusRequest request) {

        Environment env = getEnvironment(environmentId);
        String status = request.getStatus();

        if ("READY".equalsIgnoreCase(status)) {
            if (env.getStatus() != EnvironmentStatus.PROVISIONING) {
                throw new IllegalArgumentException(
                        "Environment must be in PROVISIONING status before becoming READY"
                );
            }

            env.setStatus(EnvironmentStatus.READY);
            env.setNamespace(request.getNamespace());
            env.setApplicationUrl(request.getApplicationUrl());
            env.setFailureReason(null);
            env = environmentRepository.save(env);

            sendNotification(
                    env,
                    "ENVIRONMENT_READY",
                    "Environment Ready",
                    "Your environment " + env.getApplicationName() + " is ready."
            );

            recordActivity(
                    env,
                    EnvironmentActivityType.READY,
                    "Environment is ready"
            );

        } else if ("FAILED".equalsIgnoreCase(status)) {
            if (env.getStatus() != EnvironmentStatus.PROVISIONING
                    && env.getStatus() != EnvironmentStatus.DELETING
                    && env.getStatus() != EnvironmentStatus.EXPIRED) {

                throw new IllegalArgumentException(
                        "Environment cannot be marked FAILED from status: "
                                + env.getStatus()
                );
            }

            env = markFailed(env, request.getFailureReason());

        } else if ("DELETED".equalsIgnoreCase(status)) {
            if (env.getStatus() != EnvironmentStatus.DELETING
                    && env.getStatus() != EnvironmentStatus.EXPIRED) {

                throw new IllegalArgumentException(
                        "Environment cannot be marked DELETED from status: "
                                + env.getStatus()
                );
            }

            env.setStatus(EnvironmentStatus.DELETED);
            env.setFailureReason(null);
            env = environmentRepository.save(env);

            sendNotification(
                    env,
                    "ENVIRONMENT_DELETED",
                    "Environment Deleted",
                    "Environment " + env.getApplicationName()
                            + " has been deleted."
            );

            recordActivity(
                    env,
                    EnvironmentActivityType.DELETED,
                    "Environment deleted"
            );

        } else {
            throw new IllegalArgumentException(
                    "Unsupported provisioning status: " + status
            );
        }

        return convertToDTO(env);
    }

    public List<EnvironmentActivityResponse> getEnvironmentActivity(
            String environmentId, String userId, String role) {

        Environment env = getEnvironment(environmentId);
        validateEnvironmentAccess(env, userId, role);

        return activityRepository
                .findByEnvironmentIdOrderByCreatedAtDesc(environmentId)
                .stream()
                .map(a -> new EnvironmentActivityResponse(
                        a.getActivityId(),
                        a.getEnvironmentId(),
                        a.getType(),
                        a.getMessage(),
                        a.getCreatedAt()
                ))
                .toList();
    }

    private Environment markFailed(Environment env, String reason) {
        env.setStatus(EnvironmentStatus.FAILED);
        env.setFailureReason(reason);
        env = environmentRepository.save(env);

        sendNotification(
                env,
                "ENVIRONMENT_FAILED",
                "Environment Failed",
                "Environment " + env.getApplicationName() + " failed."
        );

        recordActivity(
                env,
                EnvironmentActivityType.FAILED,
                "Environment failed: " + reason
        );

        return env;
    }

    private void recordActivity(
            Environment env,
            EnvironmentActivityType type,
            String message) {

        EnvironmentActivity activity = new EnvironmentActivity();
        activity.setEnvironmentId(env.getEnvironmentId());
        activity.setUserId(env.getUserId());
        activity.setType(type);
        activity.setMessage(message);

        activityRepository.save(activity);
    }

    private void sendNotification(
            Environment env,
            String type,
            String title,
            String message) {

        try {
            notificationServiceClient.sendNotification(
                    new NotificationServiceClient.NotificationRequest(
                            env.getUserId(),
                            env.getEnvironmentId(),
                            type,
                            title,
                            message
                    )
            );
        } catch (Exception ignored) {
        }
    }

    private Environment getEnvironment(String environmentId) {
        return environmentRepository.findById(environmentId)
                .orElseThrow(() ->
                        new EnvironmentNotFoundException(
                                "Environment not found with id: " + environmentId
                        )
                );
    }

    private void validateEnvironmentAccess(
            Environment env,
            String userId,
            String role) {

        if ("ROLE_ADMIN".equals(role)) return;

        if (!env.getUserId().equals(userId)) {
            throw new EnvironmentAccessDeniedException(
                    "You are not allowed to access this environment"
            );
        }
    }

    private void markExpirationCleanupFailed(
            Environment env,
            String reason) {

        env.setStatus(EnvironmentStatus.EXPIRED);
        env.setFailureReason(reason);
        environmentRepository.save(env);
    }

    private void validateLifetime(Integer hours) {
        if (!List.of(2, 4, 8, 24).contains(hours)) {
            throw new IllegalArgumentException(
                    "Lifetime must be one of: 2, 4, 8, 24 hours"
            );
        }
    }

    private String generateEnvironmentCode(String applicationName) {
        return applicationName.toLowerCase()
                .replace(" ", "-")
                + "-"
                + UUID.randomUUID().toString().substring(0, 5);
    }

    public DashboardSummaryResponse getDashboardSummary(
        String userId,
        String role) {

    boolean admin = "ROLE_ADMIN".equals(role);

    long total = admin
            ? environmentRepository.count()
            : environmentRepository.countByUserId(userId);

    return new DashboardSummaryResponse(
            total,
            count(userId, role, EnvironmentStatus.READY),
            count(userId, role, EnvironmentStatus.PROVISIONING),
            count(userId, role, EnvironmentStatus.FAILED),
            count(userId, role, EnvironmentStatus.EXPIRED),
            count(userId, role, EnvironmentStatus.DELETED),
            notificationServiceClient.getUnreadCount(userId)
    );
}

private long count(
        String userId,
        String role,
        EnvironmentStatus status) {

    return "ROLE_ADMIN".equals(role)
            ? environmentRepository.countByStatus(status)
            : environmentRepository
                    .countByUserIdAndStatus(userId, status);
}

    private Environment convertToEntity(CreateEnvironmentRequest request) {
        Environment env = new Environment();
        env.setApplicationName(request.getApplicationName());
        env.setTemplateId(request.getTemplateId());
        env.setEnvironmentType(request.getEnvironmentType());
        env.setRepositoryUrl(request.getRepositoryUrl());
        env.setBranchName(request.getBranchName());
        return env;
    }

    private EnvironmentResponse convertToDTO(Environment env) {
        return new EnvironmentResponse(
                env.getEnvironmentId(),
                env.getEnvironmentCode(),
                env.getApplicationName(),
                env.getUserId(),
                env.getTemplateId(),
                env.getEnvironmentType(),
                env.getStatus(),
                env.getCreatedAt(),
                env.getUpdatedAt(),
                env.getExpiresAt(),
                env.getNamespace(),
                env.getApplicationUrl(),
                env.getRepositoryUrl(),
                env.getBranchName(),
                env.getFailureReason()
        );
    }
}
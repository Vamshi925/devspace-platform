package com.devspace.environment.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devspace.environment.dto.request.CreateEnvironmentRequest;
import com.devspace.environment.dto.request.ProvisioningRequest;
import com.devspace.environment.dto.request.ProvisioningStatusRequest;
import com.devspace.environment.dto.request.DeprovisioningRequest;
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.dto.response.TemplateResponse;
import com.devspace.environment.dto.response.ProvisioningResponse;

import com.devspace.environment.exception.EnvironmentAccessDeniedException;
import com.devspace.environment.exception.EnvironmentNotFoundException; 
import com.devspace.environment.exception.ProvisioningServiceUnavailableException;
import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.repository.EnvironmentRepository;
import com.devspace.environment.client.TemplateServiceClient;
import com.devspace.environment.client.ProvisioningServiceClient;

@Service
public class EnvironmentService {

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private TemplateServiceClient templateServiceClient;

    @Autowired
    private ProvisioningServiceClient provisioningServiceClient;

    // Create Environment
    public EnvironmentResponse createEnvironment(
        CreateEnvironmentRequest request,
        String userId) {

    // Validate requested lifetime
    validateLifetime(
            request.getLifetimeHours()
    );

    // Validate template with Template Service
    TemplateResponse template =
            templateServiceClient.getTemplateById(
                    request.getTemplateId()
            );

    if (template == null ||
            Boolean.FALSE.equals(template.getActive())) {

        throw new IllegalArgumentException(
                "Selected template is not available"
        );
    }

    // Convert request DTO to entity
    Environment environment =
            convertToEntity(request);

    // Set system-controlled values
    environment.setUserId(
            userId
    );

    environment.setEnvironmentCode(
            generateEnvironmentCode(
                    request.getApplicationName()
            )
    );

    environment.setStatus(
            EnvironmentStatus.REQUESTED
    );

    environment.setExpiresAt(
            Instant.now().plus(
                    request.getLifetimeHours(),
                    ChronoUnit.HOURS
            )
    );

    // Save initial environment request
    Environment savedEnvironment =
            environmentRepository.save(
                    environment
            );

    /*
     * IMPORTANT:
     * Mark the environment as PROVISIONING before
     * calling Provisioning Service.
     *
     * Provisioning Service can callback immediately
     * and change PROVISIONING -> READY.
     */
    savedEnvironment.setStatus(
            EnvironmentStatus.PROVISIONING
    );

    savedEnvironment.setFailureReason(
            null
    );

    savedEnvironment =
            environmentRepository.save(
                    savedEnvironment
            );

    // Build request for Provisioning Service
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

        // Call Provisioning Service
        ProvisioningResponse provisioningResponse =
                provisioningServiceClient.provisionEnvironment(
                        provisioningRequest
                );

        /*
         * Do NOT set PROVISIONING again here.
         *
         * Provisioning Service may already have called
         * Environment Service and changed the environment
         * from PROVISIONING -> READY.
         */
        if (provisioningResponse == null) {

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

        } else if ("FAILED".equalsIgnoreCase(
                provisioningResponse.getStatus()
        )) {

            savedEnvironment.setStatus(
                    EnvironmentStatus.FAILED
            );

            savedEnvironment.setFailureReason(
                    provisioningResponse.getMessage()
            );

            savedEnvironment =
                    environmentRepository.save(
                            savedEnvironment
                    );

        } else if (!"ACCEPTED".equalsIgnoreCase(
                provisioningResponse.getStatus()
        )) {

            savedEnvironment.setStatus(
                    EnvironmentStatus.FAILED
            );

            savedEnvironment.setFailureReason(
                    "Provisioning Service did not accept the provisioning request"
            );

            savedEnvironment =
                    environmentRepository.save(
                            savedEnvironment
                    );
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
    }

    /*
     * Fetch the latest environment state.
     *
     * Provisioning Service may have already called:
     *
     * /internal/environments/{id}/provisioning-status
     *
     * and changed:
     *
     * PROVISIONING -> READY
     */
    Environment latestEnvironment =
            environmentRepository
                    .findById(
                            savedEnvironment.getEnvironmentId()
                    )
                    .orElse(savedEnvironment);

    return convertToDTO(
            latestEnvironment
    );
}
    // Get Environment By ID
    public EnvironmentResponse getEnvironmentById(String environmentId,String userId) {

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new EnvironmentNotFoundException(
                        "Environment not found with id: " + environmentId));

         if (!environment.getUserId().equals(userId)) {
        throw new EnvironmentAccessDeniedException(
                "You are not allowed to access this environment"
        );
    }
        return convertToDTO(environment);
    }

    // Get All Environments
    public List<EnvironmentResponse> getAllEnvironments() {

        List<Environment> environments = environmentRepository.findAll();

        return environments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Delete Environment
    public EnvironmentResponse deleteEnvironment(
        String environmentId,
        String userId) {

    Environment environment =
            environmentRepository.findById(
                    environmentId
            ).orElseThrow(
                    () -> new EnvironmentNotFoundException(
                            "Environment not found with id: "
                                    + environmentId
                    )
            );

    if (!environment.getUserId().equals(userId)) {

        throw new EnvironmentAccessDeniedException(
                "You are not allowed to delete this environment"
        );
    }

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
                "Environment cannot be deleted while provisioning is in progress"
        );
    }

    environment.setStatus(
            EnvironmentStatus.DELETING
    );

    environment.setFailureReason(
            null
    );

    environment =
            environmentRepository.save(
                    environment
            );

    DeprovisioningRequest deprovisioningRequest =
            new DeprovisioningRequest(
                    environment.getEnvironmentId(),
                    environment.getEnvironmentCode()
            );

    try {

        ProvisioningResponse response =
                provisioningServiceClient
                        .deprovisionEnvironment(
                                deprovisioningRequest
                        );

        if (response == null) {

            environment.setFailureReason(
                    "Provisioning Service returned an empty response during cleanup"
            );

        } else if ("FAILED".equalsIgnoreCase(
                response.getStatus()
        )) {

            environment.setFailureReason(
                    response.getMessage()
            );
        }

    } catch (ProvisioningServiceUnavailableException ex) {

        environment.setFailureReason(
                ex.getMessage()
        );

        environmentRepository.save(
                environment
        );

        throw ex;
    }

    Environment latestEnvironment =
            environmentRepository
                    .findById(
                            environmentId
                    )
                    .orElse(environment);

    return convertToDTO(
            latestEnvironment
    );
}

    // Generate Environment Code
    private String generateEnvironmentCode(String applicationName) {

        String randomPart = UUID.randomUUID()
                .toString()
                .substring(0, 5);

        return applicationName
                .toLowerCase()
                .replace(" ", "-")
                + "-"
                + randomPart;
    }

    // Validate Environment Lifetime
    private void validateLifetime(Integer lifetimeHours) {

        List<Integer> allowedLifetimes = List.of(2, 4, 8, 24);

        if (!allowedLifetimes.contains(lifetimeHours)) {
            throw new IllegalArgumentException(
                    "Lifetime must be one of: 2, 4, 8, 24 hours");
        }
    }

    // Get Environments By User ID
    public List<EnvironmentResponse> getEnvironmentsByUserId(String userId) {

        List<Environment> environments =
                environmentRepository.findEnvironmentsByUserId(userId);

        return environments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //extend environment
    public EnvironmentResponse extendEnvironment(
        String environmentId,
        String userId,
        Integer additionalHours) {

    validateLifetime(additionalHours);

    Environment environment = environmentRepository.findById(environmentId)
            .orElseThrow(() ->
                    new EnvironmentNotFoundException(
                            "Environment not found with id: " + environmentId
                    )
            );

    if (!environment.getUserId().equals(userId)) {
        throw new EnvironmentAccessDeniedException(
                "You are not allowed to extend this environment"
        );
    }

    if (environment.getStatus() != EnvironmentStatus.READY) {
        throw new IllegalArgumentException(
                "Environment can only be extended while in READY status"
        );
    }

    environment.setExpiresAt(
            environment.getExpiresAt().plus(
                    additionalHours,
                    ChronoUnit.HOURS
            )
    );

    Environment updatedEnvironment =
            environmentRepository.save(environment);

    return convertToDTO(updatedEnvironment);
}

//expire environment
public void expireEnvironment(String environmentId) {

    Environment environment =
            environmentRepository.findById(environmentId)
                    .orElseThrow(() ->
                            new EnvironmentNotFoundException(
                                    "Environment not found with id: "
                                            + environmentId
                            )
                    );

    if (environment.getStatus()
            != EnvironmentStatus.READY) {

        return;
    }

    if (environment.getExpiresAt()
            .isAfter(Instant.now())) {

        return;
    }

    // Step 1: Mark environment as expired
    environment.setStatus(
            EnvironmentStatus.EXPIRED
    );

    environment.setFailureReason(
            null
    );

    environment =
            environmentRepository.save(
                    environment
            );

    // Step 2: Move to deletion lifecycle
    environment.setStatus(
            EnvironmentStatus.DELETING
    );

    environment =
            environmentRepository.save(
                    environment
            );

    // Step 3: Ask Provisioning Service to clean up
    DeprovisioningRequest deprovisioningRequest =
            new DeprovisioningRequest(
                    environment.getEnvironmentId(),
                    environment.getEnvironmentCode()
            );

    try {

        ProvisioningResponse response =
                provisioningServiceClient
                        .deprovisionEnvironment(
                                deprovisioningRequest
                        );

        if (response == null) {

            environment.setFailureReason(
                    "Provisioning Service returned an empty response during expiration cleanup"
            );

            environmentRepository.save(
                    environment
            );

        } else if ("FAILED".equalsIgnoreCase(
                response.getStatus())) {

            environment.setFailureReason(
                    response.getMessage()
            );

            environmentRepository.save(
                    environment
            );
        }

    } catch (ProvisioningServiceUnavailableException ex) {

        environment.setFailureReason(
                ex.getMessage()
        );

        environmentRepository.save(
                environment
        );

        throw ex;
    }
}

public EnvironmentResponse updateProvisioningStatus(
        String environmentId,
        ProvisioningStatusRequest request) {

    Environment environment =
            environmentRepository.findById(environmentId)
                    .orElseThrow(() ->
                            new EnvironmentNotFoundException(
                                    "Environment not found with id: "
                                            + environmentId
                            )
                    );

    // PROVISIONING -> READY
    if ("READY".equalsIgnoreCase(
            request.getStatus())) {

        if (environment.getStatus()
                != EnvironmentStatus.PROVISIONING) {

            throw new IllegalArgumentException(
                    "Environment must be in PROVISIONING status before becoming READY"
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

        environment.setFailureReason(
                null
        );

    // PROVISIONING / DELETING / EXPIRED -> FAILED
    } else if ("FAILED".equalsIgnoreCase(
            request.getStatus())) {

        if (environment.getStatus()
                != EnvironmentStatus.PROVISIONING
                &&
            environment.getStatus()
                != EnvironmentStatus.DELETING
                &&
            environment.getStatus()
                != EnvironmentStatus.EXPIRED) {

            throw new IllegalArgumentException(
                    "Environment cannot be marked FAILED from status: "
                            + environment.getStatus()
            );
        }

        environment.setStatus(
                EnvironmentStatus.FAILED
        );

        environment.setFailureReason(
                request.getFailureReason()
        );

    // DELETING / EXPIRED -> DELETED
    } else if ("DELETED".equalsIgnoreCase(
            request.getStatus())) {

        if (environment.getStatus()
                != EnvironmentStatus.DELETING
                &&
            environment.getStatus()
                != EnvironmentStatus.EXPIRED) {

            throw new IllegalArgumentException(
                    "Environment cannot be marked DELETED from status: "
                            + environment.getStatus()
            );
        }

        environment.setStatus(
                EnvironmentStatus.DELETED
        );

        environment.setFailureReason(
                null
        );

    } else {

        throw new IllegalArgumentException(
                "Unsupported provisioning status: "
                        + request.getStatus()
        );
    }

    Environment updatedEnvironment =
            environmentRepository.save(
                    environment
            );

    return convertToDTO(
            updatedEnvironment
    );
}

    // Convert DTO to Entity
    private Environment convertToEntity(CreateEnvironmentRequest request) {

        Environment environment = new Environment();

        environment.setApplicationName(request.getApplicationName());
        environment.setTemplateId(request.getTemplateId());
        environment.setEnvironmentType(request.getEnvironmentType());
        environment.setRepositoryUrl(request.getRepositoryUrl());
        environment.setBranchName(request.getBranchName());

        return environment;
    }

    // Convert Entity to DTO
    private EnvironmentResponse convertToDTO(Environment environment) {

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
                environment.getFailureReason());
    }
}


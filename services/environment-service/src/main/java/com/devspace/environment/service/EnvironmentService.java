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
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.exception.EnvironmentAccessDeniedException;
import com.devspace.environment.exception.EnvironmentNotFoundException;
import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.repository.EnvironmentRepository;

@Service
public class EnvironmentService {

    @Autowired
    private EnvironmentRepository environmentRepository;

    // Create Environment
    public EnvironmentResponse createEnvironment(
            CreateEnvironmentRequest request,
            String userId) {

        validateLifetime(request.getLifetimeHours());

        Environment environment = convertToEntity(request);

        environment.setUserId(userId);

        environment.setEnvironmentCode(
                generateEnvironmentCode(request.getApplicationName()));

        environment.setStatus(EnvironmentStatus.REQUESTED);

        environment.setExpiresAt(
                Instant.now().plus(
                        request.getLifetimeHours(),
                        ChronoUnit.HOURS));

        Environment savedEnvironment = environmentRepository.save(environment);

        return convertToDTO(savedEnvironment);
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

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new EnvironmentNotFoundException(
                        "Environment not found with id: " + environmentId));

        if (!environment.getUserId().equals(userId)) {
            throw new EnvironmentAccessDeniedException(
                    "You are not allowed to delete this environment");
        }

        if (environment.getStatus() == EnvironmentStatus.DELETING ||
        environment.getStatus() == EnvironmentStatus.DELETED ||
        environment.getStatus() == EnvironmentStatus.PROVISIONING) {

        throw new IllegalArgumentException(
                "Environment cannot be deleted while in status: "
                        + environment.getStatus()
        );
    }

        environment.setStatus(EnvironmentStatus.DELETING);

        Environment updatedEnvironment = environmentRepository.save(environment);

        return convertToDTO(updatedEnvironment);
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
@Transactional
public void expireEnvironment(String environmentId) {

    Environment environment = environmentRepository.findById(environmentId)
            .orElseThrow(() ->
                    new EnvironmentNotFoundException(
                            "Environment not found with id: " + environmentId
                    )
            );

    if (environment.getStatus() != EnvironmentStatus.READY) {
        return;
    }

    if (environment.getExpiresAt().isAfter(Instant.now())) {
        return;
    }

    environment.setStatus(EnvironmentStatus.EXPIRED);
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


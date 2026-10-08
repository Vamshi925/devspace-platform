package com.devspace.environment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devspace.environment.client.GitHubRepositoryClient;
import com.devspace.environment.client.ProvisioningServiceClient;
import com.devspace.environment.client.TemplateServiceClient;
import com.devspace.environment.dto.request.CreateEnvironmentRequest;
import com.devspace.environment.dto.request.DeprovisioningRequest;
import com.devspace.environment.dto.request.ProvisioningRequest;
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.dto.response.ProvisioningResponse;
import com.devspace.environment.dto.response.TemplateResponse;
import com.devspace.environment.exception.EnvironmentAccessDeniedException;
import com.devspace.environment.exception.EnvironmentNotFoundException;
import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.model.EnvironmentType;
import com.devspace.environment.repository.EnvironmentRepository;

@ExtendWith(MockitoExtension.class)
class EnvironmentServiceApplicationTests {

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private ProvisioningServiceClient provisioningServiceClient;

    @Mock
    private TemplateServiceClient templateServiceClient;

    @Mock
    private GitHubRepositoryClient gitHubRepositoryClient;

    @InjectMocks
    private EnvironmentService environmentService;

    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new Environment();
        environment.setEnvironmentId("env-123");
        environment.setEnvironmentCode("payment-service-a1234");
        environment.setApplicationName("payment-service");
        environment.setUserId("user-001");
        environment.setTemplateId("template-123");
        environment.setEnvironmentType(EnvironmentType.DEVELOPMENT);
        environment.setStatus(EnvironmentStatus.READY);
        environment.setCreatedAt(Instant.now());
        environment.setUpdatedAt(Instant.now());
        environment.setExpiresAt(Instant.now().plusSeconds(3600));
    }

    @Test
    void shouldCreateEnvironment() {
        CreateEnvironmentRequest request = createRequest();
        TemplateResponse template = createTemplate();

        when(templateServiceClient.getTemplateById("template-123"))
                .thenReturn(template);

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation -> {
                    Environment saved = invocation.getArgument(0);

                    if (saved.getEnvironmentId() == null) {
                        saved.setEnvironmentId("env-123");
                    }

                    return saved;
                });

        ProvisioningResponse provisioningResponse =
                new ProvisioningResponse();

        provisioningResponse.setEnvironmentId("env-123");
        provisioningResponse.setStatus("ACCEPTED");
        provisioningResponse.setMessage(
                "Provisioning request accepted"
        );

        when(provisioningServiceClient.provisionEnvironment(
                any(ProvisioningRequest.class)))
                .thenReturn(provisioningResponse);

        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.empty());

        EnvironmentResponse response =
                environmentService.createEnvironment(
                        request,
                        "user-001"
                );

        assertEquals(
                "payment-service",
                response.getApplicationName()
        );

        assertEquals(
                "user-001",
                response.getUserId()
        );

        assertEquals(
                EnvironmentStatus.PROVISIONING,
                response.getStatus()
        );

        verify(templateServiceClient)
                .getTemplateById("template-123");

        verify(provisioningServiceClient)
                .provisionEnvironment(
                        any(ProvisioningRequest.class)
                );

        verify(environmentRepository, times(2))
                .save(any(Environment.class));

        verify(gitHubRepositoryClient)
                .validateRepositoryAndBranch(
                        "https://github.com/example/payment-service",
                        "main"
                );
    }

    @Test
    void shouldRejectInvalidLifetime() {
        CreateEnvironmentRequest request = createRequest();
        request.setLifetimeHours(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> environmentService.createEnvironment(
                        request,
                        "user-001"
                )
        );

        verify(environmentRepository, never())
                .save(any(Environment.class));
    }

    @Test
    void shouldGetEnvironmentById() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        EnvironmentResponse response =
                environmentService.getEnvironmentById(
                        "env-123",
                        "user-001",
                        "ROLE_USER"
                );

        assertEquals(
                "env-123",
                response.getEnvironmentId()
        );

        assertEquals(
                "payment-service",
                response.getApplicationName()
        );
    }

    @Test
    void shouldAllowAdminToAccessAnyEnvironment() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        EnvironmentResponse response =
                environmentService.getEnvironmentById(
                        "env-123",
                        "admin-001",
                        "ROLE_ADMIN"
                );

        assertEquals(
                "env-123",
                response.getEnvironmentId()
        );
    }

    @Test
    void shouldThrowExceptionWhenEnvironmentNotFound() {
        when(environmentRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        assertThrows(
                EnvironmentNotFoundException.class,
                () -> environmentService.getEnvironmentById(
                        "invalid-id",
                        "user-001",
                        "ROLE_USER"
                )
        );
    }

    @Test
    void shouldRejectAccessForDifferentUser() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        assertThrows(
                EnvironmentAccessDeniedException.class,
                () -> environmentService.getEnvironmentById(
                        "env-123",
                        "user-002",
                        "ROLE_USER"
                )
        );
    }

    @Test
    void shouldMarkEnvironmentAsDeleting() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        EnvironmentResponse response =
                environmentService.deleteEnvironment(
                        "env-123",
                        "user-001",
                        "ROLE_USER"
                );

        assertEquals(
                EnvironmentStatus.DELETING,
                response.getStatus()
        );
    }

    @Test
    void shouldRejectDeleteWhenAlreadyDeleting() {
        environment.setStatus(EnvironmentStatus.DELETING);

        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        assertThrows(
                IllegalArgumentException.class,
                () -> environmentService.deleteEnvironment(
                        "env-123",
                        "user-001",
                        "ROLE_USER"
                )
        );
    }

    @Test
    void shouldRejectDeleteForDifferentUser() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        assertThrows(
                EnvironmentAccessDeniedException.class,
                () -> environmentService.deleteEnvironment(
                        "env-123",
                        "user-002",
                        "ROLE_USER"
                )
        );
    }

    @Test
    void shouldAllowAdminToDeleteAnyEnvironment() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        EnvironmentResponse response =
                environmentService.deleteEnvironment(
                        "env-123",
                        "admin-001",
                        "ROLE_ADMIN"
                );

        assertEquals(
                EnvironmentStatus.DELETING,
                response.getStatus()
        );
    }

    @Test
    void shouldExtendReadyEnvironment() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Instant oldExpiry =
                environment.getExpiresAt();

        EnvironmentResponse response =
                environmentService.extendEnvironment(
                        "env-123",
                        "user-001",
                        "ROLE_USER",
                        4
                );

        assertEquals(
                oldExpiry.plusSeconds(4 * 60 * 60),
                response.getExpiresAt()
        );
    }

    @Test
    void shouldRejectExtensionWhenEnvironmentNotReady() {
        environment.setStatus(EnvironmentStatus.DELETING);

        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        assertThrows(
                IllegalArgumentException.class,
                () -> environmentService.extendEnvironment(
                        "env-123",
                        "user-001",
                        "ROLE_USER",
                        4
                )
        );
    }

    @Test
    void shouldRejectExtensionForDifferentUser() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        assertThrows(
                EnvironmentAccessDeniedException.class,
                () -> environmentService.extendEnvironment(
                        "env-123",
                        "user-002",
                        "ROLE_USER",
                        4
                )
        );
    }

    @Test
    void shouldAllowAdminToExtendAnyEnvironment() {
        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Instant oldExpiry =
                environment.getExpiresAt();

        EnvironmentResponse response =
                environmentService.extendEnvironment(
                        "env-123",
                        "admin-001",
                        "ROLE_ADMIN",
                        4
                );

        assertEquals(
                oldExpiry.plusSeconds(4 * 60 * 60),
                response.getExpiresAt()
        );
    }

    @Test
    void shouldDeleteEnvironmentSuccessfully() {
        ProvisioningResponse provisioningResponse =
                new ProvisioningResponse();

        provisioningResponse.setEnvironmentId("env-123");
        provisioningResponse.setStatus("DELETED");
        provisioningResponse.setMessage(
                "Environment cleanup completed"
        );

        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment))
                .thenAnswer(invocation -> {
                    environment.setStatus(
                            EnvironmentStatus.DELETED
                    );

                    return Optional.of(environment);
                });

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(provisioningServiceClient.deprovisionEnvironment(
                any(DeprovisioningRequest.class)))
                .thenReturn(provisioningResponse);

        EnvironmentResponse response =
                environmentService.deleteEnvironment(
                        "env-123",
                        "user-001",
                        "ROLE_USER"
                );

        verify(provisioningServiceClient)
                .deprovisionEnvironment(
                        any(DeprovisioningRequest.class)
                );

        assertEquals(
                EnvironmentStatus.DELETED,
                response.getStatus()
        );
    }

    @Test
    void shouldRejectEnvironmentWhenGitHubRepositoryOrBranchIsInvalid() {
        CreateEnvironmentRequest request =
                createRequest();

        when(templateServiceClient.getTemplateById("template-123"))
                .thenReturn(createTemplate());

        doThrow(
                new IllegalArgumentException(
                        "GitHub repository or branch not found"
                )
        ).when(gitHubRepositoryClient)
                .validateRepositoryAndBranch(
                        request.getRepositoryUrl(),
                        request.getBranchName()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> environmentService.createEnvironment(
                                request,
                                "user-001"
                        )
                );

        assertEquals(
                "GitHub repository or branch not found",
                exception.getMessage()
        );

        verify(environmentRepository, never())
                .save(any(Environment.class));

        verify(provisioningServiceClient, never())
                .provisionEnvironment(
                        any(ProvisioningRequest.class)
                );
    }

    private CreateEnvironmentRequest createRequest() {
        CreateEnvironmentRequest request =
                new CreateEnvironmentRequest();

        request.setApplicationName("payment-service");
        request.setTemplateId("template-123");
        request.setEnvironmentType(
                EnvironmentType.DEVELOPMENT
        );
        request.setLifetimeHours(8);
        request.setRepositoryUrl(
                "https://github.com/example/payment-service"
        );
        request.setBranchName("main");

        return request;
    }

    private TemplateResponse createTemplate() {
        TemplateResponse template =
                new TemplateResponse();

        template.setTemplateId("template-123");
        template.setName("spring-postgres");
        template.setActive(true);
        template.setContainerImage("nginx:alpine");
        template.setApplicationPort(80);
        template.setCpuRequest("100m");
        template.setCpuLimit("500m");
        template.setMemoryRequest("128Mi");
        template.setMemoryLimit("512Mi");

        return template;
    }
}
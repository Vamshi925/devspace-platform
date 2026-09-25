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

import com.devspace.environment.dto.request.CreateEnvironmentRequest;
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.exception.EnvironmentAccessDeniedException;
import com.devspace.environment.exception.EnvironmentNotFoundException;
import com.devspace.environment.model.Environment;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.model.EnvironmentType;
import com.devspace.environment.repository.EnvironmentRepository;

@ExtendWith(MockitoExtension.class)
public class EnvironmentServiceApplicationTests {

    @Mock
    private EnvironmentRepository environmentRepository;

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
        environment.setTemplateId(1L);
        environment.setEnvironmentType(EnvironmentType.DEVELOPMENT);
        environment.setStatus(EnvironmentStatus.READY);
        environment.setCreatedAt(Instant.now());
        environment.setUpdatedAt(Instant.now());
        environment.setExpiresAt(Instant.now().plusSeconds(3600));
    }

    @Test
    void shouldCreateEnvironment() {

        CreateEnvironmentRequest request =
                new CreateEnvironmentRequest();

        request.setApplicationName("payment-service");
        request.setTemplateId(1L);
        request.setEnvironmentType(EnvironmentType.DEVELOPMENT);
        request.setLifetimeHours(8);
        request.setRepositoryUrl("https://github.com/example/payment-service");
        request.setBranchName("main");

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnvironmentResponse response =
                environmentService.createEnvironment(request, "user-001");

        assertEquals("payment-service", response.getApplicationName());
        assertEquals("user-001", response.getUserId());
        assertEquals(EnvironmentStatus.REQUESTED, response.getStatus());

        verify(environmentRepository, times(1))
                .save(any(Environment.class));
    }

    @Test
    void shouldRejectInvalidLifetime() {

        CreateEnvironmentRequest request =
                new CreateEnvironmentRequest();

        request.setApplicationName("payment-service");
        request.setTemplateId(1L);
        request.setEnvironmentType(EnvironmentType.DEVELOPMENT);
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
                        "user-001"
                );

        assertEquals("env-123", response.getEnvironmentId());
        assertEquals("payment-service", response.getApplicationName());
    }

    @Test
    void shouldThrowExceptionWhenEnvironmentNotFound() {

        when(environmentRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        assertThrows(
                EnvironmentNotFoundException.class,
                () -> environmentService.getEnvironmentById(
                        "invalid-id",
                        "user-001"
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
                        "user-002"
                )
        );
    }

    @Test
    void shouldMarkEnvironmentAsDeleting() {

        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnvironmentResponse response =
                environmentService.deleteEnvironment(
                        "env-123",
                        "user-001"
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
                        "user-001"
                )
        );
    }

    @Test
    void shouldExtendReadyEnvironment() {

        when(environmentRepository.findById("env-123"))
                .thenReturn(Optional.of(environment));

        when(environmentRepository.save(any(Environment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant oldExpiry = environment.getExpiresAt();

        EnvironmentResponse response =
                environmentService.extendEnvironment(
                        "env-123",
                        "user-001",
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
                        4
                )
        );
    }
}
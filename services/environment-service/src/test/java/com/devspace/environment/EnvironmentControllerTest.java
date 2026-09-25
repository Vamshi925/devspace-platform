package com.devspace.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.exception.EnvironmentAccessDeniedException;
import com.devspace.environment.exception.EnvironmentNotFoundException;
import com.devspace.environment.model.EnvironmentStatus;
import com.devspace.environment.model.EnvironmentType;
import com.devspace.environment.service.EnvironmentService;

@WebMvcTest(EnvironmentController.class)
public class EnvironmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvironmentService environmentService;

    private EnvironmentResponse createResponse() {

        return new EnvironmentResponse(
                "env-123",
                "payment-service-a1234",
                "payment-service",
                "user-001",
                1L,
                EnvironmentType.DEVELOPMENT,
                EnvironmentStatus.REQUESTED,
                Instant.now(),
                Instant.now(),
                Instant.now().plusSeconds(28800),
                null,
                null,
                "https://github.com/example/payment-service",
                "main",
                null
        );
    }

    @Test
    void shouldCreateEnvironment() throws Exception {

        when(environmentService.createEnvironment(any(), anyString()))
                .thenReturn(createResponse());

        mockMvc.perform(post("/api/environments")
                        .header("X-User-Id", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationName": "payment-service",
                                  "templateId": 1,
                                  "environmentType": "DEVELOPMENT",
                                  "lifetimeHours": 8,
                                  "repositoryUrl": "https://github.com/example/payment-service",
                                  "branchName": "main"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationName")
                        .value("payment-service"))
                .andExpect(jsonPath("$.userId")
                        .value("user-001"))
                .andExpect(jsonPath("$.status")
                        .value("REQUESTED"));
    }

    @Test
    void shouldReturnValidationErrorWhenApplicationNameMissing()
            throws Exception {

        mockMvc.perform(post("/api/environments")
                        .header("X-User-Id", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": 1,
                                  "environmentType": "DEVELOPMENT",
                                  "lifetimeHours": 8
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message")
                        .value("Application name is required"));
    }

    @Test
    void shouldGetEnvironmentById() throws Exception {

        when(environmentService.getEnvironmentById(
                "env-123",
                "user-001"
        )).thenReturn(createResponse());

        mockMvc.perform(get("/api/environments/env-123")
                        .header("X-User-Id", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environmentId")
                        .value("env-123"))
                .andExpect(jsonPath("$.applicationName")
                        .value("payment-service"));
    }

    @Test
    void shouldReturn404WhenEnvironmentNotFound()
            throws Exception {

        when(environmentService.getEnvironmentById(
                "invalid-id",
                "user-001"
        )).thenThrow(
                new EnvironmentNotFoundException(
                        "Environment not found with id: invalid-id"
                )
        );

        mockMvc.perform(get("/api/environments/invalid-id")
                        .header("X-User-Id", "user-001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("ENVIRONMENT_NOT_FOUND"));
    }

    @Test
    void shouldReturn403WhenAccessDenied()
            throws Exception {

        when(environmentService.getEnvironmentById(
                "env-123",
                "user-002"
        )).thenThrow(
                new EnvironmentAccessDeniedException(
                        "You are not allowed to access this environment"
                )
        );

        mockMvc.perform(get("/api/environments/env-123")
                        .header("X-User-Id", "user-002"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error")
                        .value("ACCESS_DENIED"));
    }

    @Test
    void shouldGetMyEnvironments() throws Exception {

        when(environmentService.getEnvironmentsByUserId("user-001"))
                .thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/environments/my")
                        .header("X-User-Id", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId")
                        .value("user-001"));
    }

    @Test
    void shouldDeleteEnvironment() throws Exception {

        EnvironmentResponse response = createResponse();
        response.setStatus(EnvironmentStatus.DELETING);

        when(environmentService.deleteEnvironment(
                "env-123",
                "user-001"
        )).thenReturn(response);

        mockMvc.perform(delete("/api/environments/env-123")
                        .header("X-User-Id", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("DELETING"));
    }

    @Test
    void shouldExtendEnvironment() throws Exception {

        EnvironmentResponse response = createResponse();
        response.setStatus(EnvironmentStatus.READY);

        when(environmentService.extendEnvironment(
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn(response);

        mockMvc.perform(patch("/api/environments/env-123/extend")
                        .header("X-User-Id", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "additionalHours": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("READY"));
    }
}
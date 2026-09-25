package com.devspace.environment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.devspace.environment.dto.request.CreateEnvironmentRequest;
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.service.EnvironmentService;
import com.devspace.environment.dto.request.ExtendEnvironmentRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

        @Autowired
        private EnvironmentService environmentService;

        // Create Environment
        @PostMapping
        public ResponseEntity<EnvironmentResponse> createEnvironment(
                        @Valid @RequestBody CreateEnvironmentRequest request,
                        @RequestHeader("X-User-Id") String userId) {

                EnvironmentResponse environment = environmentService.createEnvironment(request, userId);

                return new ResponseEntity<>(environment, HttpStatus.CREATED);
        }

        // Get All Environments
        @GetMapping
        public ResponseEntity<List<EnvironmentResponse>> getAllEnvironments() {

                List<EnvironmentResponse> environments = environmentService.getAllEnvironments();

                return ResponseEntity.ok(environments);
        }

        // Get Environment By ID
        @GetMapping("/{environmentId}")
public ResponseEntity<EnvironmentResponse> getEnvironmentById(
        @PathVariable String environmentId,
        @RequestHeader("X-User-Id") String userId) {

    EnvironmentResponse environment =
            environmentService.getEnvironmentById(environmentId, userId);

    return ResponseEntity.ok(environment);
}

        @DeleteMapping("/{environmentId}")
public ResponseEntity<EnvironmentResponse> deleteEnvironment(
        @PathVariable String environmentId,
        @RequestHeader("X-User-Id") String userId) {

    EnvironmentResponse environment =
            environmentService.deleteEnvironment(environmentId, userId);

    return ResponseEntity.ok(environment);
}

@PatchMapping("/{environmentId}/extend")
public ResponseEntity<EnvironmentResponse> extendEnvironment(
        @PathVariable String environmentId,
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody ExtendEnvironmentRequest request) {

    EnvironmentResponse environment =
            environmentService.extendEnvironment(
                    environmentId,
                    userId,
                    request.getAdditionalHours()
            );

    return ResponseEntity.ok(environment);
}


        // Get Current User Environments
        @GetMapping("/my")
        public ResponseEntity<List<EnvironmentResponse>> getMyEnvironments(
                        @RequestHeader("X-User-Id") String userId) {

                List<EnvironmentResponse> environments = environmentService.getEnvironmentsByUserId(userId);

                return ResponseEntity.ok(environments);
        }
}

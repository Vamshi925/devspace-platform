package com.devspace.environment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.devspace.environment.dto.request.ProvisioningStatusRequest;
import com.devspace.environment.dto.response.EnvironmentResponse;
import com.devspace.environment.service.EnvironmentService;

@RestController
@RequestMapping("/internal/environments")
public class InternalEnvironmentController {

    private final EnvironmentService environmentService;

    public InternalEnvironmentController(
            EnvironmentService environmentService) {

        this.environmentService = environmentService;
    }

    @PatchMapping("/{environmentId}/provisioning-status")
    public ResponseEntity<EnvironmentResponse> updateProvisioningStatus(
            @PathVariable String environmentId,
            @RequestBody ProvisioningStatusRequest request) {

        EnvironmentResponse response =
                environmentService.updateProvisioningStatus(
                        environmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}
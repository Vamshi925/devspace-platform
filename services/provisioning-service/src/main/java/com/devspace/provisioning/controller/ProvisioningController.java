package com.devspace.provisioning.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devspace.provisioning.dto.request.DeprovisioningRequest;
import com.devspace.provisioning.dto.request.ProvisioningRequest;
import com.devspace.provisioning.dto.response.ProvisioningResponse;
import com.devspace.provisioning.service.ProvisioningService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/provisioning")
public class ProvisioningController {

    private final ProvisioningService provisioningService;

    public ProvisioningController(
            ProvisioningService provisioningService) {

        this.provisioningService =
                provisioningService;
    }

    @PostMapping
    public ResponseEntity<ProvisioningResponse> provisionEnvironment(
            @Valid @RequestBody ProvisioningRequest request) {

        ProvisioningResponse response =
                provisioningService.provisionEnvironment(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @DeleteMapping
    public ResponseEntity<ProvisioningResponse> deprovisionEnvironment(
            @Valid @RequestBody DeprovisioningRequest request) {

        ProvisioningResponse response =
                provisioningService.deprovisionEnvironment(
                        request
                );

        return ResponseEntity
                .ok(response);
    }
}
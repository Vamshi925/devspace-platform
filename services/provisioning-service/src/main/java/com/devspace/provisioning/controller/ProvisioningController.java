package com.devspace.provisioning.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        this.provisioningService = provisioningService;
    }

    @PostMapping
    public ResponseEntity<ProvisioningResponse> provisionEnvironment(
            @Valid @RequestBody ProvisioningRequest request) {

        ProvisioningResponse response =
                provisioningService.provisionEnvironment(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.ACCEPTED
        );
    }
}
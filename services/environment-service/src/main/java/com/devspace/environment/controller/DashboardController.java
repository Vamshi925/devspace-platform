package com.devspace.environment.controller;

import com.devspace.environment.dto.response.DashboardSummaryResponse;
import com.devspace.environment.service.EnvironmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final EnvironmentService environmentService;

    public DashboardController(
            EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(
                environmentService.getDashboardSummary(
                        userId,
                        role
                )
        );
    }
}
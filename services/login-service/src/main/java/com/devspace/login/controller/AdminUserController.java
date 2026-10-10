package com.devspace.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devspace.login.dto.request.UpdateUserRoleRequest;
import com.devspace.login.dto.response.UserRoleResponse;
import com.devspace.login.service.AdminUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(
            AdminUserService adminUserService) {

        this.adminUserService = adminUserService;
    }

    @PatchMapping("/role")
    public ResponseEntity<UserRoleResponse>
            updateUserRole(
                    @Valid
                    @RequestBody
                    UpdateUserRoleRequest request) {

        UserRoleResponse response =
                adminUserService.updateUserRole(
                        request
                );

        return ResponseEntity.ok(response);
    }
}
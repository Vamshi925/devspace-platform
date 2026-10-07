package com.devspace.login.service;

import org.springframework.stereotype.Service;

import com.devspace.login.dto.request.UpdateUserRoleRequest;
import com.devspace.login.dto.response.UserRoleResponse;
import com.devspace.login.model.Role;
import com.devspace.login.model.User;
import com.devspace.login.repository.RoleRepository;
import com.devspace.login.repository.UserRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserService(
            UserRepository userRepository,
            RoleRepository roleRepository) {

        this.userRepository =
                userRepository;

        this.roleRepository =
                roleRepository;
    }

    public UserRoleResponse updateUserRole(
            String userId,
            UpdateUserRoleRequest request) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found with id: "
                                                + userId
                                )
                        );

        String requestedRole =
                request.getRole()
                        .trim()
                        .toUpperCase();

        if (!requestedRole.equals("ROLE_USER")
                &&
                !requestedRole.equals("ROLE_ADMIN")) {

            throw new IllegalArgumentException(
                    "Supported roles are ROLE_USER and ROLE_ADMIN"
            );
        }

        Role role =
                roleRepository.findByRoleName(
                        requestedRole
                ).orElseThrow(() ->
                        new IllegalStateException(
                                "Role not configured: "
                                        + requestedRole
                        )
                );

        user.setRole(
                role
        );

        User updatedUser =
                userRepository.save(
                        user
                );

        return new UserRoleResponse(
                updatedUser.getUserId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
                        .getRoleName()
        );
    }
}
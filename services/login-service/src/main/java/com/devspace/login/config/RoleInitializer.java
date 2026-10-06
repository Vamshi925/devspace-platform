package com.devspace.login.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.devspace.login.model.Role;
import com.devspace.login.repository.RoleRepository;

@Component
public class RoleInitializer
        implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(
            RoleRepository roleRepository) {

        this.roleRepository =
                roleRepository;
    }

    @Override
    public void run(
            String... args) {

        createRoleIfNotFound(
                "ROLE_USER"
        );

        createRoleIfNotFound(
                "ROLE_ADMIN"
        );
    }

    private void createRoleIfNotFound(
            String roleName) {

        if (roleRepository
                .findByRoleName(roleName)
                .isPresent()) {

            return;
        }

        Role role =
                new Role();

        role.setRoleName(
                roleName
        );

        roleRepository.save(
                role
        );
    }
}
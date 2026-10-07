package com.devspace.login.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.devspace.login.model.Role;
import com.devspace.login.model.User;
import com.devspace.login.repository.RoleRepository;
import com.devspace.login.repository.UserRepository;

@Component
public class AdminBootstrapInitializer
        implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${devspace.admin.email:}")
    private String adminEmail;

    public AdminBootstrapInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository) {

        this.userRepository =
                userRepository;

        this.roleRepository =
                roleRepository;
    }

    @Override
    public void run(
            String... args) {

        if (adminEmail == null
                ||
                adminEmail.isBlank()) {

            return;
        }

        userRepository.findByEmail(
                adminEmail.trim().toLowerCase()
        ).ifPresent(user -> {

            Role adminRole =
                    roleRepository.findByRoleName(
                            "ROLE_ADMIN"
                    ).orElseThrow(() ->
                            new IllegalStateException(
                                    "ROLE_ADMIN is not configured"
                            )
                    );

            if (!"ROLE_ADMIN".equals(
                    user.getRole()
                            .getRoleName())) {

                user.setRole(
                        adminRole
                );

                userRepository.save(
                        user
                );
            }
        });
    }
}
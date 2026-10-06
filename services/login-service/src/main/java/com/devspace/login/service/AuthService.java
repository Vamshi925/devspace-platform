package com.devspace.login.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import com.devspace.login.dto.request.LoginRequest;
import com.devspace.login.dto.request.RegisterRequest;

import com.devspace.login.dto.response.AuthResponse;
import com.devspace.login.dto.response.RegisterResponse;

import com.devspace.login.model.Role;
import com.devspace.login.model.User;

import com.devspace.login.repository.RoleRepository;
import com.devspace.login.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository =
                userRepository;

        this.roleRepository =
                roleRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.authenticationManager =
                authenticationManager;

        this.jwtService =
                jwtService;
    }

    // Register User
    public RegisterResponse register(
            RegisterRequest request) {

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "User with this email already exists"
            );
        }

        if (userRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {

            throw new IllegalArgumentException(
                    "User with this phone number already exists"
            );
        }

        Role role =
                roleRepository
                        .findByRoleName(
                                "ROLE_USER"
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "ROLE_USER is not configured"
                                        )
                        );

        User user =
                new User();

        user.setName(
                request.getName()
        );

        user.setEmail(
                request.getEmail()
                        .toLowerCase()
                        .trim()
        );

        user.setPhoneNumber(
                request.getPhoneNumber()
        );

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(
                role
        );

        User savedUser =
                userRepository.save(
                        user
                );

        return new RegisterResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getRole()
                        .getRoleName()
        );
    }

    // Login User
    public AuthResponse login(
            LoginRequest request) {

        String email =
                request.getEmail()
                        .toLowerCase()
                        .trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user =
                userRepository
                        .findByEmail(
                                email
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "User not found"
                                        )
                        );

        String token =
                jwtService.generateToken(
                        user.getUserId(),
                        user.getEmail(),
                        user.getRole()
                                .getRoleName()
                );

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
                        .getRoleName()
        );
    }
}
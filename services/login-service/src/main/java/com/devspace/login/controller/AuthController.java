package com.devspace.login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devspace.login.dto.request.LoginRequest;
import com.devspace.login.dto.request.RegisterRequest;

import com.devspace.login.dto.response.AuthResponse;
import com.devspace.login.dto.response.RegisterResponse;

import com.devspace.login.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService) {

        this.authService =
                authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse>
            register(
                    @Valid
                    @RequestBody
                    RegisterRequest request) {

        RegisterResponse response =
                authService.register(
                        request
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        response
                );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse>
            login(
                    @Valid
                    @RequestBody
                    LoginRequest request) {

        AuthResponse response =
                authService.login(
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }
}
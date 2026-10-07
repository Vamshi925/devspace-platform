package com.devspace.login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;

    public SecurityConfiguration(
            UserDetailsService userDetailsService) {

        this.userDetailsService =
                userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider
            authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    @Bean
    public AuthenticationManager
            authenticationManager(
                    AuthenticationConfiguration configuration)
                    throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain
            securityFilterChain(
                    HttpSecurity http)
                    throws Exception {

        return http

                .csrf(
                        csrf ->
                                csrf.disable()
                )

                .authorizeHttpRequests(
                        auth ->
                                auth
                                        .requestMatchers(
                                                "/api/auth/register",
                                                "/api/auth/login",
                                                "/api/admin/**",
                                                "/actuator/**"
                                        )
                                        .permitAll()

                                        .anyRequest()
                                        .authenticated()
                )

                .sessionManagement(
                        session ->
                                session
                                        .sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS
                                        )
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .build();
    }
}
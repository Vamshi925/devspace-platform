package com.devspace.provisioning.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class InternalApiSecurityFilter
        extends OncePerRequestFilter {

    @Value("${devspace.internal.api-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path =
                request.getRequestURI();

        if (path.startsWith(
                "/actuator")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String providedKey =
                request.getHeader(
                        "X-Internal-Api-Key"
                );

        if (!internalApiKey.equals(
                providedKey)) {

            response.sendError(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid internal API key"
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}
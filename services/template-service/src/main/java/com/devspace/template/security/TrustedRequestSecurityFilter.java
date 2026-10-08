package com.devspace.template.security;

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
public class TrustedRequestSecurityFilter
        extends OncePerRequestFilter {

    @Value("${devspace.gateway.internal-key}")
    private String gatewayInternalKey;

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

        String gatewayKey =
                request.getHeader(
                        "X-DevSpace-Gateway-Key"
                );

        String internalKey =
                request.getHeader(
                        "X-Internal-Api-Key"
                );

        boolean validGateway =
                gatewayInternalKey.equals(
                        gatewayKey
                );

        boolean validInternal =
                internalApiKey.equals(
                        internalKey
                );

        if (!validGateway
                &&
                !validInternal) {

            response.sendError(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Untrusted request"
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}
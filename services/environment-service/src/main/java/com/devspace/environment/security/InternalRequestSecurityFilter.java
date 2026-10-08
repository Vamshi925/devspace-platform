package com.devspace.environment.security;

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
public class InternalRequestSecurityFilter
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

        // Health endpoints remain accessible
        if (path.startsWith(
                "/actuator")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        // Internal service-to-service endpoints
        if (path.startsWith(
                "/internal/")) {

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

            return;
        }

        // Normal Environment APIs must come
        // through API Gateway
        String providedGatewayKey =
                request.getHeader(
                        "X-DevSpace-Gateway-Key"
                );

        if (!gatewayInternalKey.equals(
                providedGatewayKey)) {

            response.sendError(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Request must pass through API Gateway"
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}
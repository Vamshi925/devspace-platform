package com.devspace.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrustedRequestSecurityFilter
        extends OncePerRequestFilter {

    @Value("${devspace.gateway.internal-key}")
    private String gatewayKey;

    @Value("${devspace.internal.api-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/internal/")) {
            String providedInternalKey =
                    request.getHeader("X-Internal-Api-Key");

            if (!internalApiKey.equals(providedInternalKey)) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED
                );
                return;
            }

            filterChain.doFilter(request, response);
            return;
        }

        String providedGatewayKey =
                request.getHeader("X-DevSpace-Gateway-Key");

        if (!gatewayKey.equals(providedGatewayKey)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
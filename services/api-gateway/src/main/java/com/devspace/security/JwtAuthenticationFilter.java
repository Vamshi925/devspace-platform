package com.devspace.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.springframework.core.Ordered;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.http.server.reactive.ServerHttpRequest;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService =
                jwtService;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path =
                exchange.getRequest()
                        .getURI()
                        .getPath();

        HttpMethod method =
                exchange.getRequest()
                        .getMethod();

        // Public APIs
        if (isPublicEndpoint(path)) {

            return chain.filter(exchange);
        }

        String authHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst("Authorization");

        if (authHeader == null
                ||
                !authHeader.startsWith("Bearer ")) {

            return unauthorized(exchange);
        }

        String token =
                authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {

            return unauthorized(exchange);
        }

        String userId =
                jwtService.extractUserId(token);

        String role =
                jwtService.extractRole(token);

        if (userId == null
                ||
                userId.isBlank()
                ||
                role == null
                ||
                role.isBlank()) {

            return unauthorized(exchange);
        }

        // ----------------------------
        // Authorization
        // ----------------------------

        if (requiresAdmin(path, method)
                &&
                !"ROLE_ADMIN".equals(role)) {

            return forbidden(exchange);
        }

        // ----------------------------
        // Trusted identity headers
        // ----------------------------

        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .headers(headers -> {

                            // Remove anything supplied
                            // by the client
                            headers.remove("X-User-Id");
                            headers.remove("X-User-Role");
                        })
                        .header(
                                "X-User-Id",
                                userId
                        )
                        .header(
                                "X-User-Role",
                                role
                        )
                        .build();

        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(request)
                        .build();

        return chain.filter(
                mutatedExchange
        );
    }

    private boolean isPublicEndpoint(
            String path) {

        return path.equals(
                        "/api/auth/login"
                )
                ||
                path.equals(
                        "/api/auth/register"
                )
                ||
                path.startsWith(
                        "/actuator"
                );
    }

    private boolean requiresAdmin(
            String path,
            HttpMethod method) {

        
         if (path.startsWith(
            "/api/admin/")) {

        return true;
    }

        // Only admins can view every environment
        if (path.equals("/api/environments")
                &&
                method == HttpMethod.GET) {

            return true;
        }

        // Template reads are allowed
        // for normal authenticated users.
        //
        // Template modification is admin-only.
        if (path.startsWith("/api/templates")) {

            return method == HttpMethod.POST
                    ||
                    method == HttpMethod.PUT
                    ||
                    method == HttpMethod.PATCH
                    ||
                    method == HttpMethod.DELETE;
        }

        return false;
    }

    private Mono<Void> unauthorized(
            ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(
                        HttpStatus.UNAUTHORIZED
                );

        return exchange.getResponse()
                .setComplete();
    }

    private Mono<Void> forbidden(
            ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(
                        HttpStatus.FORBIDDEN
                );

        return exchange.getResponse()
                .setComplete();
    }

    @Override
    public int getOrder() {

        return -100;
    }
}
package com.devspace.gateway.security;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.springframework.http.server.reactive.ServerHttpRequest;

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

        // Public endpoints
        if (isPublicEndpoint(path)) {

            return chain.filter(
                    exchange
            );
        }

        String authHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                "Authorization"
                        );

        if (authHeader == null
                ||
                !authHeader.startsWith(
                        "Bearer "
                )) {

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.UNAUTHORIZED
                    );

            return exchange.getResponse()
                    .setComplete();
        }

        String token =
                authHeader.substring(7);

        if (!jwtService.isTokenValid(
                token)) {

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.UNAUTHORIZED
                    );

            return exchange.getResponse()
                    .setComplete();
        }

        String userId =
                jwtService.extractUserId(
                        token
                );

        String role =
                jwtService.extractRole(
                        token
                );

        if (userId == null
                ||
                userId.isBlank()) {

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.UNAUTHORIZED
                    );

            return exchange.getResponse()
                    .setComplete();
        }

        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()

                        // Never trust client-supplied identity.
                        .headers(
                                headers -> {

                                    headers.remove(
                                            "X-User-Id"
                                    );

                                    headers.remove(
                                            "X-User-Role"
                                    );
                                }
                        )

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
                        .request(
                                request
                        )
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

    @Override
    public int getOrder() {

        return -100;
    }
}
package com.devspace.gateway.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalRequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalRequestLoggingFilter.class);

    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-Id";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .header(
                                CORRELATION_ID_HEADER,
                                correlationId
                        )
                        .build();

        ServerWebExchange updatedExchange =
                exchange.mutate()
                        .request(request)
                        .build();

        String method =
                request.getMethod().toString();

        String path =
                request.getURI().getPath();

        String userId =
                request.getHeaders()
                        .getFirst("X-User-Id");

        String finalCorrelationId = correlationId;

        logger.info(
                "Incoming request - CorrelationId: {}, Method: {}, Path: {}, UserId: {}",
                finalCorrelationId,
                method,
                path,
                userId
        );

        updatedExchange.getResponse()
                .getHeaders()
                .set(
                        CORRELATION_ID_HEADER,
                        finalCorrelationId
                );

        return chain.filter(updatedExchange)
                .then(
                        Mono.fromRunnable(() -> {

                            int status =
                                    updatedExchange.getResponse()
                                            .getStatusCode() != null
                                            ? updatedExchange.getResponse()
                                                    .getStatusCode()
                                                    .value()
                                            : 0;

                            logger.info(
                                    "Completed request - CorrelationId: {}, Method: {}, Path: {}, Status: {}",
                                    finalCorrelationId,
                                    method,
                                    path,
                                    status
                            );
                        })
                );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
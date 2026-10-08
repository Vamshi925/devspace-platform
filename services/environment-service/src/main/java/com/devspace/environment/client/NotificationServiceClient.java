package com.devspace.environment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationServiceClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public NotificationServiceClient(
            @Value("${devspace.notification-service.url}")
            String notificationServiceUrl,
            @Value("${devspace.internal.api-key}")
            String internalApiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();

        this.internalApiKey = internalApiKey;
    }

    public void sendNotification(NotificationRequest request) {
        restClient.post()
                .uri("/internal/notifications")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public record NotificationRequest(
            String userId,
            String environmentId,
            String type,
            String title,
            String message) {
    }
}
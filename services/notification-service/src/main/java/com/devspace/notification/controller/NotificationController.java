package com.devspace.notification.controller;

import com.devspace.notification.dto.request.CreateNotificationRequest;
import com.devspace.notification.dto.response.NotificationResponse;
import com.devspace.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/internal/notifications")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        return ResponseEntity.ok(
                notificationService.createNotification(request)
        );
    }

    @GetMapping("/api/notifications/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByUser(userId)
        );
    }

    @PatchMapping("/api/notifications/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable String notificationId,
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        notificationId,
                        userId
                )
        );
    }
}
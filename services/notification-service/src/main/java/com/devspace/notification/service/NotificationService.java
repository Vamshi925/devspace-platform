package com.devspace.notification.service;

import com.devspace.notification.exception.NotificationAccessDeniedException;
import com.devspace.notification.exception.NotificationNotFoundException;
import com.devspace.notification.dto.request.CreateNotificationRequest;
import com.devspace.notification.dto.response.NotificationResponse;
import com.devspace.notification.model.Notification;
import com.devspace.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse createNotification(
            CreateNotificationRequest request) {

        Notification notification = new Notification();

        notification.setUserId(request.getUserId());
        notification.setEnvironmentId(request.getEnvironmentId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setRead(false);

        return convertToResponse(
                notificationRepository.save(notification)
        );
    }

    public List<NotificationResponse> getNotificationsByUser(
            String userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public NotificationResponse markAsRead(
            String notificationId,
            String userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found"
                        )
                );

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException(
                    "You are not allowed to access this notification"
            );
        }

        notification.setRead(true);

        return convertToResponse(
                notificationRepository.save(notification)
        );
    }

    private NotificationResponse convertToResponse(
            Notification notification) {

        NotificationResponse response =
                new NotificationResponse();

        response.setNotificationId(
                notification.getNotificationId()
        );
        response.setUserId(notification.getUserId());
        response.setEnvironmentId(
                notification.getEnvironmentId()
        );
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());

        return response;
    }
}
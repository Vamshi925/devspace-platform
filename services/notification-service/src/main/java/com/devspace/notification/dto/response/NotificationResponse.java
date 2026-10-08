package com.devspace.notification.dto.response;

import com.devspace.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String notificationId;
    private String userId;
    private String environmentId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;
}
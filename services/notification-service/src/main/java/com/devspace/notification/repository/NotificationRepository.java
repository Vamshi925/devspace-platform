package com.devspace.notification.repository;

import com.devspace.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            String userId
    );
}
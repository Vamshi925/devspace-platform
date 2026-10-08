package com.devspace.notification.exception;

public class NotificationAccessDeniedException extends RuntimeException {

    public NotificationAccessDeniedException(String message) {
        super(message);
    }
}
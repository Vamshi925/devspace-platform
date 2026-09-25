package com.devspace.environment.exception;

public class EnvironmentAccessDeniedException extends RuntimeException {

    public EnvironmentAccessDeniedException(String message) {
        super(message);
    }
}
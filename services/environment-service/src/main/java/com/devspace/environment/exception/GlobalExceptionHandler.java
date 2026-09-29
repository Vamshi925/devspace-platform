package com.devspace.environment.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devspace.environment.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Environment Not Found
    @ExceptionHandler(EnvironmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEnvironmentNotFound(
            EnvironmentNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "ENVIRONMENT_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Invalid Business Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Bean Validation Error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Generic Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Something went wrong",
                request.getRequestURI()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(EnvironmentAccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDenied(
        EnvironmentAccessDeniedException ex,
        HttpServletRequest request) {

    ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.FORBIDDEN.value(),
            "ACCESS_DENIED",
            ex.getMessage(),
            request.getRequestURI()
    );

    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
}

     @ExceptionHandler(TemplateNotFoundException.class)
public ResponseEntity<ErrorResponse> handleTemplateNotFound(
        TemplateNotFoundException ex,
        HttpServletRequest request) {

    ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "TEMPLATE_NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI()
    );

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
}

@ExceptionHandler(TemplateServiceUnavailableException.class)
public ResponseEntity<ErrorResponse> handleTemplateServiceUnavailable(
        TemplateServiceUnavailableException ex,
        HttpServletRequest request) {

    ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "TEMPLATE_SERVICE_UNAVAILABLE",
            ex.getMessage(),
            request.getRequestURI()
    );

    return new ResponseEntity<>(
            error,
            HttpStatus.SERVICE_UNAVAILABLE
    );
}
}
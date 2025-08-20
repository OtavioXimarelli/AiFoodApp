package com.otavio.aifoodapp.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for all API endpoints
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle all uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "internal_server_error",
                    "message", "An unexpected error occurred",
                    "path", request.getDescription(false).replace("uri=", ""),
                    "status", 500
                ));
    }
    
    /**
     * Handle security-related exceptions
     */
    @ExceptionHandler({
        org.springframework.security.access.AccessDeniedException.class,
        org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
        org.springframework.security.authentication.BadCredentialsException.class,
        org.springframework.security.core.AuthenticationException.class
    })
    public ResponseEntity<?> handleSecurityExceptions(Exception ex, WebRequest request) {
        log.warn("Security exception: {}", ex.getMessage(), ex);
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "unauthorized",
                    "message", "Authentication required",
                    "path", request.getDescription(false).replace("uri=", ""),
                    "status", 401
                ));
    }
}

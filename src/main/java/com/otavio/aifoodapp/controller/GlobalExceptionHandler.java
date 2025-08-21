package com.otavio.aifoodapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.otavio.aifoodapp.exception.UsernameOrPasswordInvalidException;

import lombok.extern.slf4j.Slf4j;

/**
 * Manipulador global consolidado de exceções para todos os endpoints da API
 * Combina funcionalidades de validação, negócio e segurança
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Tratar exceções de usuário não encontrado
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "user_not_found",
                    "message", ex.getMessage(),
                    "path", request.getDescription(false).replace("uri=", ""),
                    "status", 400
                ));
    }

    /**
     * Tratar exceções de usuário/senha inválidos
     */
    @ExceptionHandler(UsernameOrPasswordInvalidException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleUsernameOrPasswordInvalidException(UsernameOrPasswordInvalidException ex, WebRequest request) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "invalid_credentials",
                    "message", ex.getMessage(),
                    "path", request.getDescription(false).replace("uri=", ""),
                    "status", 401
                ));
    }

    /**
     * Tratar exceções de validação de argumentos
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            FieldError fieldError = (FieldError) error;
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", "validation_failed",
                    "message", "Request validation failed",
                    "fieldErrors", fieldErrors,
                    "path", request.getDescription(false).replace("uri=", ""),
                    "status", 400
                ));
    }

    /**
     * Tratar exceções relacionadas à segurança
     */
    @ExceptionHandler({
        org.springframework.security.access.AccessDeniedException.class,
        org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
        org.springframework.security.authentication.BadCredentialsException.class,
        org.springframework.security.core.AuthenticationException.class
    })
    public ResponseEntity<Map<String, Object>> handleSecurityExceptions(Exception ex, WebRequest request) {
        log.warn("Security exception: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "unauthorized",
                    "message", "Authentication required",
                    "path", request.getDescription(false).replace("uri=", ""),
                    "status", 401
                ));
    }

    /**
     * Tratar todas as exceções não capturadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request) {
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
}

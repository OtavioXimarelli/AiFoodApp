package com.otavio.aifoodapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health check controller
 * Use this to check if the application is running correctly
 */
@RestController
public class HealthController {

    /**
     * Basic health check endpoint
     * This endpoint is publicly accessible and doesn't require authentication
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "AiFoodApp",
            "timestamp", System.currentTimeMillis()
        ));
    }
}

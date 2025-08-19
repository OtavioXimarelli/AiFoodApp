package com.otavio.aifoodapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * This controller handles duplicate API endpoints when the frontend accidentally sends requests with /api/api/ prefix
 * It forwards these requests to the proper controllers
 */
@RestController
@RequestMapping("/api/api")
@Slf4j
public class ApiRedirectController {

    private final AuthController authController;
    
    public ApiRedirectController(AuthController authController) {
        this.authController = authController;
    }
    
    /**
     * Redirect /api/api/auth/status to /api/auth/status
     */
    @GetMapping("/auth/status")
    public ResponseEntity<?> redirectAuthStatus(HttpServletRequest request) {
        log.info("Redirecting duplicate API path /api/api/auth/status to /api/auth/status");
        return authController.authStatus(request);
    }
}

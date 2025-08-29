package com.otavio.aifoodapp.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Simplified rate limiting filter to prevent system overload
 * Uses a simple time-window approach instead of complex scheduling
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    // Simple time-window based request tracking
    private final Map<String, RequestWindow> requestWindows = new ConcurrentHashMap<>();
    
    // Maximum requests per minute per IP
    private static final int MAX_REQUESTS_PER_MINUTE = 300;
    private static final int MAX_SENSITIVE_REQUESTS_PER_MINUTE = 60;
    
    // Window size in milliseconds (1 minute)
    private static final long WINDOW_SIZE_MS = 60_000;
    
    // Paths exempt from rate limiting
    private static final List<String> EXEMPT_PATHS = Arrays.asList(
        "/oauth2/authorization",
        "/login/oauth2/code/",
        "/error",
        "/api/auth/status",
        "/api/auth",
        "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String path = request.getServletPath();
        
        // Check if path is exempt
        boolean isExempt = EXEMPT_PATHS.stream().anyMatch(path::startsWith);
        
        // Check for authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                  !"anonymousUser".equals(auth.getPrincipal());
        
        if (isExempt || (isAuthenticated && path.startsWith("/api/auth/"))) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Clean expired windows periodically (1% chance)
        if (Math.random() < 0.01) {
            cleanExpiredWindows();
        }
        
        // Check rate limit
        if (isRateLimited(clientIp, path)) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientIp, String path) {
        RequestWindow window = requestWindows.computeIfAbsent(clientIp, k -> new RequestWindow());
        
        long now = System.currentTimeMillis();
        boolean isSensitive = isSensitiveEndpoint(path);
        int maxRequests = isSensitive ? MAX_SENSITIVE_REQUESTS_PER_MINUTE : MAX_REQUESTS_PER_MINUTE;
        
        return window.isRateLimited(now, maxRequests);
    }
    
    private boolean isSensitiveEndpoint(String path) {
        return path.startsWith("/api/auth/") || 
               path.contains("/login") || 
               path.contains("/oauth2") ||
               path.endsWith("/save") ||
               path.endsWith("/delete") ||
               path.endsWith("/update");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    private void cleanExpiredWindows() {
        long now = System.currentTimeMillis();
        requestWindows.entrySet().removeIf(entry -> 
            now - entry.getValue().windowStart > WINDOW_SIZE_MS * 2);
    }
    
    /**
     * Simple time window for tracking requests
     */
    private static class RequestWindow {
        private long windowStart;
        private int requestCount;
        
        public synchronized boolean isRateLimited(long now, int maxRequests) {
            // Reset window if expired
            if (now - windowStart > WINDOW_SIZE_MS) {
                windowStart = now;
                requestCount = 0;
            }
            
            requestCount++;
            return requestCount > maxRequests;
        }
    }
}
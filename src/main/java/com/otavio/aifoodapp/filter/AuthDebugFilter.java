package com.otavio.aifoodapp.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter to log debug information about requests and sessions.
 * This helps diagnose authentication issues.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Execute right after SameSiteCookieFilter
@Slf4j
public class AuthDebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            String uri = httpRequest.getRequestURI();
            
            // Only log debug info for important auth endpoints
            if (uri.contains("/auth/") || uri.contains("/login") || uri.contains("/oauth2/")) {
                logRequestDetails(httpRequest);
            }
        }
        
        // Continue processing the request
        chain.doFilter(request, response);
    }
    
    private void logRequestDetails(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");
        
        log.debug("Auth request - URI: {}, Method: {}", uri, method);
        log.debug("Auth request - Referer: {}, Origin: {}", referer, origin);
        log.debug("Auth request - User-Agent: {}", userAgent);
        
        // Log session info if available
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.debug("Auth request - Session ID: {}, Creation Time: {}, Last Accessed: {}", 
                    session.getId(),
                    new java.util.Date(session.getCreationTime()),
                    new java.util.Date(session.getLastAccessedTime()));
            
            try {
                Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
                log.debug("Auth request - Security context in session: {}", 
                        securityContext != null ? "Present" : "Missing");
            } catch (Exception e) {
                log.warn("Error checking security context in session", e);
            }
        } else {
            log.debug("Auth request - No session found");
        }
        
        // Log cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.debug("Auth request - {} cookies found", cookies.length);
            for (Cookie cookie : cookies) {
                log.debug("Cookie: {}={}, Domain: {}, Path: {}, MaxAge: {}, Secure: {}, HttpOnly: {}",
                        cookie.getName(), 
                        cookie.getValue().substring(0, Math.min(8, cookie.getValue().length())) + "...",
                        cookie.getDomain() != null ? cookie.getDomain() : "default",
                        cookie.getPath(),
                        cookie.getMaxAge(),
                        cookie.getSecure(),
                        cookie.isHttpOnly());
            }
        } else {
            log.debug("Auth request - No cookies found");
        }
    }
}

package com.otavio.aifoodapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Deprecated no-op filter kept for compatibility.
 * JWT/token handling has been removed in favor of OAuth2 login with server-side sessions.
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    public SecurityFilter() {
        // no-op
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Just pass the request through; no JWT/token processing anymore
        logger.trace("SecurityFilter (no-op) passing through request to {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
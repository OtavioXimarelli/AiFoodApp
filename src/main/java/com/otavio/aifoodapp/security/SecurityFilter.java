package com.otavio.aifoodapp.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Deprecated no-op filter kept for compatibility.
 * JWT/token handling has been removed in favor of OAuth2 login with server-side sessions.
 */
@Component
public class SecurityFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Just pass the request through; no JWT/token processing anymore
        if (request instanceof HttpServletRequest httpRequest) {
            LOG.trace("SecurityFilter (no-op) passing through request to {}", httpRequest.getRequestURI());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
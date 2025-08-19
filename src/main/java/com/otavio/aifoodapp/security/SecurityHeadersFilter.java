package com.otavio.aifoodapp.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para adicionar cabeçalhos de segurança às respostas
 * Ajuda a prevenir ataques comuns como XSS, clickjacking, etc.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Content Security Policy - prevenir XSS
        StringBuilder cspBuilder = new StringBuilder();
        cspBuilder.append("default-src 'self'; ");
        cspBuilder.append("img-src 'self' data: https://www.googleapis.com https://*.googleusercontent.com; ");
        cspBuilder.append("script-src 'self' 'unsafe-inline' 'unsafe-eval' https://apis.google.com; ");
        cspBuilder.append("style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; ");
        cspBuilder.append("font-src 'self' https://fonts.gstatic.com; ");
        cspBuilder.append("frame-src 'self' https://accounts.google.com; ");
        cspBuilder.append("connect-src 'self' ").append(frontendUrl).append(" https://www.googleapis.com https://accounts.google.com;");

        // Adicionar cabeçalhos de segurança apenas para respostas HTML ou JSON
        String contentType = response.getContentType();
        if (contentType == null || 
            contentType.contains("text/html") || 
            contentType.contains("application/json")) {
            
            // Desativar em ambiente de desenvolvimento se necessário (remover em produção)
            boolean isDevelopment = !Boolean.parseBoolean(System.getProperty("production", "false"));
            
            if (!isDevelopment) {
                response.setHeader("Content-Security-Policy", cspBuilder.toString());
            }
            
            // Prevenir clickjacking
            response.setHeader("X-Frame-Options", "DENY");
            
            // Forçar HTTPS - desativar apenas em desenvolvimento
            if (!isDevelopment) {
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }
            
            // Prevenir MIME type sniffing
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // Habilitar proteção XSS em navegadores antigos
            response.setHeader("X-XSS-Protection", "1; mode=block");
            
            // Política de referrer
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        }
        
        filterChain.doFilter(request, response);
    }
}

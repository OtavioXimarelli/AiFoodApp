package com.otavio.aifoodapp.security;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para corrigir URLs com prefixo de API duplicado até que o frontend seja atualizado.
 * Este filtro detecta paths como /api/api/... e os corrige internamente para /api/...
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Execute logo após o RateLimitingFilter
@Slf4j
public class ApiPathFixFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request, 
            @org.springframework.lang.NonNull HttpServletResponse response, 
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        // Verificar se o path tem API duplicado
        if (path.startsWith("/api/api/")) {
            // Wrap o request para corrigir o path
            // Criar uma versão do request com o path corrigido
            String correctedPath = path.replaceFirst("/api/api/", "/api/");
            
            // Log detalhado para ajudar no diagnóstico
            log.debug("Corrigindo path duplicado: {} -> {}", path, correctedPath);
            if (request.getSession(false) != null) {
                log.debug("Sessão ID ao corrigir path: {}, Auth: {}", 
                    request.getSession().getId(),
                    SecurityContextHolder.getContext().getAuthentication() != null ? 
                        SecurityContextHolder.getContext().getAuthentication().getName() : "nenhuma");
            }
            
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getServletPath() {
                    return correctedPath;
                }
                
                @Override
                public String getRequestURI() {
                    return getContextPath() + getServletPath();
                }
            };
            
            log.debug("Corrigindo path duplicado: {} -> {}", path, wrappedRequest.getServletPath());
            filterChain.doFilter(wrappedRequest, response);
            return;
        }
        
        // Continuar normalmente para outros paths
        filterChain.doFilter(request, response);
    }
    
    /**
     * Wrapper simples para HttpServletRequest que permite sobrescrever o path
     */
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        public HttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }
    }
}

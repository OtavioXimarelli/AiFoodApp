package com.otavio.aifoodapp.security;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para diagnóstico de autenticação e sessão
 * Registra informações detalhadas sobre o estado de autenticação em cada requisição
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2) // Executar após RateLimitingFilter e ApiPathFixFilter
@Slf4j
public class AuthenticationDiagnosticsFilter extends OncePerRequestFilter {

    private static final List<String> IMPORTANT_PATHS = Arrays.asList(
        "/api/auth",
        "/api/api/auth",
        "/api/auth/status",
        "/api/api/auth/status",
        "/api/user",
        "/api/api/user",
        "/login",
        "/oauth2"
    );
    
    private static final List<String> IGNORED_PATHS = Arrays.asList(
        "/api/debug",
        "/css/",
        "/js/",
        "/images/",
        "/favicon.ico",
        "/static/"
    );

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        // Não logar recursos estáticos e endpoints de debug
        boolean isIgnoredPath = IGNORED_PATHS.stream().anyMatch(path::startsWith);
        if (isIgnoredPath) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Registrar informações detalhadas apenas para caminhos importantes
        boolean isImportantPath = IMPORTANT_PATHS.stream().anyMatch(path::startsWith);
        
        if (isImportantPath) {
            logAuthenticationDetails(request);
        }
        
        // Continuar com o processamento da requisição
        filterChain.doFilter(request, response);
        
        // Logar o código de status para caminhos importantes
        if (isImportantPath) {
            log.debug("🌟 Resposta para {}: Status {}", path, response.getStatus());
        }
    }
    
    /**
     * Registra informações detalhadas sobre autenticação e sessão
     */
    private void logAuthenticationDetails(HttpServletRequest request) {
        String path = request.getServletPath();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false);
        
        StringBuilder logBuilder = new StringBuilder()
            .append("\n🔍 DIAGNÓSTICO DE AUTENTICAÇÃO 🔍\n")
            .append("Path: ").append(path).append("\n")
            .append("Método: ").append(request.getMethod()).append("\n")
            .append("Sessão: ").append(session != null ? "ATIVA (ID=" + session.getId() + ")" : "NENHUMA").append("\n");
        
        if (auth != null) {
            logBuilder.append("Autenticação: ").append(auth.getName()).append("\n")
                      .append("Tipo de autenticação: ").append(auth.getClass().getSimpleName()).append("\n")
                      .append("Autenticado: ").append(auth.isAuthenticated()).append("\n")
                      .append("Autoridades: ").append(auth.getAuthorities()).append("\n");
            
            Object principal = auth.getPrincipal();
            logBuilder.append("Principal: ").append(principal instanceof String ? principal : principal.getClass().getSimpleName()).append("\n");
        } else {
            logBuilder.append("Autenticação: NENHUMA\n");
        }
        
        // Informações sobre cookies
        logBuilder.append("Cookies:\n");
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    logBuilder.append("  - ").append(cookie.getName())
                              .append(": Domain=").append(cookie.getDomain() != null ? cookie.getDomain() : "default")
                              .append(", Path=").append(cookie.getPath())
                              .append(", MaxAge=").append(cookie.getMaxAge())
                              .append(", Secure=").append(cookie.getSecure())
                              .append(", HttpOnly=").append(cookie.isHttpOnly())
                              .append("\n");
                } else {
                    logBuilder.append("  - ").append(cookie.getName()).append(" (presente)\n");
                }
            }
        } else {
            logBuilder.append("  Nenhum cookie\n");
        }
        
        // Headers importantes
        logBuilder.append("Headers:\n")
                  .append("  - User-Agent: ").append(request.getHeader("User-Agent")).append("\n")
                  .append("  - Referer: ").append(request.getHeader("Referer")).append("\n")
                  .append("  - Origin: ").append(request.getHeader("Origin")).append("\n")
                  .append("  - Accept: ").append(request.getHeader("Accept")).append("\n");
        
        log.debug(logBuilder.toString());
    }
}

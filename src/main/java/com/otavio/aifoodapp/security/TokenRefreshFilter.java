package com.otavio.aifoodapp.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter to check and refresh OAuth2 tokens if needed
 */
@Component
@Slf4j
public class TokenRefreshFilter extends OncePerRequestFilter {
    
    private final TokenService tokenService;
    
    // Caminhos públicos que não devem passar pelo filtro de refresh de token
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
        "/oauth2/", 
        "/login/",
        "/error",
        "/api/auth/status",
        "/api/auth/refresh",
        "/api/foods/test-auth",
        "/favicon.ico",
        "/oauth2/authorization/",
        "/login/oauth2/code/"
    ));
    
    // Cache para controlar a frequência de refreshes por sessão
    private final ConcurrentHashMap<String, Long> lastRefreshAttempts = new ConcurrentHashMap<>();
    
    // Tempo mínimo entre tentativas de refresh (5 segundos)
    private static final long REFRESH_THROTTLE_MS = 5000;
    
    public TokenRefreshFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // Verificar se há um header para pular a verificação de token
        if ("true".equals(request.getHeader("X-No-Token-Refresh"))) {
            log.debug("Pulando verificação de token devido ao header X-No-Token-Refresh");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Limitar frequência de verificações de token por sessão
        HttpSession session = request.getSession(false);
        String sessionId = session != null ? session.getId() : request.getRemoteAddr();
        
        long currentTime = System.currentTimeMillis();
        Long lastAttempt = lastRefreshAttempts.get(sessionId);
        
        // Se a última verificação foi há menos de 5 segundos, pule
        if (lastAttempt != null && currentTime - lastAttempt < REFRESH_THROTTLE_MS) {
            log.debug("Pulando verificação de token - última verificação foi há menos de 5 segundos");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Atualizar timestamp da última verificação
        lastRefreshAttempts.put(sessionId, currentTime);
        
        // Limpar entradas antigas a cada 100 requisições (probabilidade 1%)
        if (Math.random() < 0.01) {
            cleanOldEntries();
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Only process OAuth2 authentication tokens
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            try {
                // Try to refresh the token if needed
                boolean refreshed = tokenService.refreshAccessTokenIfNeeded(oauth2Auth);
                if (refreshed) {
                    log.debug("Successfully refreshed access token during request");
                }
            } catch (Exception e) {
                log.error("Error refreshing token: {}", e.getMessage());
                // Continue with the request even if token refresh fails
            }
        }
        
        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }
    
    /**
     * Limpa entradas antigas do cache de verificações de token
     */
    private void cleanOldEntries() {
        long currentTime = System.currentTimeMillis();
        long threshold = currentTime - (REFRESH_THROTTLE_MS * 10); // 10x o tempo de throttle
        
        lastRefreshAttempts.entrySet().removeIf(entry -> entry.getValue() < threshold);
    }
    
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Skip refresh for authentication endpoints to avoid loops
        String path = request.getRequestURI();
        
        // Verificar caminhos públicos definidos
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        
        // Não aplicar em requisições OPTIONS (CORS preflight)
        return "OPTIONS".equals(request.getMethod());
    }
}

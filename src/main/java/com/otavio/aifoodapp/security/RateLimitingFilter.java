package com.otavio.aifoodapp.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para limitar a taxa de requisições e evitar sobrecargas do sistema
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    // Mapa para contar requisições por IP
    private final Map<String, Integer> requestCountsByIp = new ConcurrentHashMap<>();
    
    // Mapa para contar requisições por IP e caminho
    private final Map<String, Map<String, Integer>> requestCountsByIpAndPath = new ConcurrentHashMap<>();
    
    // Lista de IPs temporariamente bloqueados
    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();
    
    // Número máximo de requisições permitidas por IP em um intervalo de tempo
    private static final int MAX_REQUESTS_PER_MINUTE = 300; // 5 requisições por segundo
    
    // Número máximo de requisições permitidas para endpoints sensíveis
    private static final int MAX_REQUESTS_SENSITIVE_ENDPOINT = 60; // Por minuto (1 por segundo)
    
    // Tempo de bloqueio em minutos para IPs que excedem o limite
    private static final int BLOCK_TIME_MINUTES = 3;
    
    // Executor para limpeza periódica dos contadores
    private ScheduledExecutorService scheduler;
    
    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Agendar limpeza dos contadores a cada minuto
        scheduler.scheduleAtFixedRate(this::resetCounters, 1, 1, TimeUnit.MINUTES);
        
        // Agendar limpeza dos IPs bloqueados a cada 5 minutos
        scheduler.scheduleAtFixedRate(this::cleanBlockedIps, 5, 5, TimeUnit.MINUTES);
    }
    
    @PreDestroy
    @Override
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    // frontendUrl removido pois não era utilizado

    // Lista de caminhos que devem ser ignorados pelo rate limiting
    private static final List<String> EXEMPT_PATHS = Arrays.asList(
        "/oauth2/authorization",
        "/login/oauth2/code/",
        "/error",
        "/api/auth/status",
        "/api/api/auth/status",
        "/api/auth",
        "/api/api/auth",
        "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request, 
            @org.springframework.lang.NonNull HttpServletResponse response, 
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String path = request.getServletPath();
        
        // Verificar se a sessão está ativa para usuários autenticados
        HttpSession session = request.getSession(false);
        boolean hasActiveSession = session != null;
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                  !"anonymousUser".equals(auth.getPrincipal());
        
        // Log para diagnóstico de sessões
        if (isAuthRelatedPath(path)) {
            logSessionDiagnostics(request, path, auth, session);
        }
        
        // Verificar e ajustar caminhos com API duplicada antes de aplicar limitação
        if (path.startsWith("/api/api/")) {
            // Não modificamos o path aqui, apenas registramos para fins de log
            log.debug("Detectado caminho com API duplicada: {} - será processado pelo ApiPathFixFilter", path);
        }
        
        // Ignorar completamente rotas essenciais para OAuth2 e sessão
        boolean isExempt = EXEMPT_PATHS.stream().anyMatch(path::startsWith);
        
        // Adiciona exceção para usuários autenticados em determinados endpoints
        if ((isAuthenticated && path.startsWith("/api/auth/")) || isExempt) {
            log.debug("Ignorando limite de taxa para caminho isento: {} (auth: {})", path, isAuthenticated);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Verificar se o IP está bloqueado
        if (isBlocked(clientIp)) {
            log.warn("Requisição bloqueada: IP {} está temporariamente bloqueado", clientIp);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limit_exceeded\",\"message\":\"Muitas requisições. Tente novamente mais tarde.\"}");
            return;
        }
        
        // Reduzir limites para usuários autenticados com sessão ativa
        int effectiveGlobalLimit = hasActiveSession ? MAX_REQUESTS_PER_MINUTE * 2 : MAX_REQUESTS_PER_MINUTE;
        int effectiveSensitiveLimit = hasActiveSession ? MAX_REQUESTS_SENSITIVE_ENDPOINT * 2 : MAX_REQUESTS_SENSITIVE_ENDPOINT;
        
        // Incrementar contadores globais por IP
        int count = incrementCounter(clientIp);
        
        // Verificar se excedeu o limite global
        if (count > effectiveGlobalLimit) {
            blockIp(clientIp);
            log.warn("IP {} bloqueado por exceder o limite global de requisições: {}", clientIp, count);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limit_exceeded\",\"message\":\"Muitas requisições. Tente novamente mais tarde.\"}");
            return;
        }
        
        // Verificar endpoints sensíveis com maior restrição
        if (isSensitiveEndpoint(path)) {
            int pathCount = incrementPathCounter(clientIp, path);
            
            if (pathCount > effectiveSensitiveLimit) {
                blockIp(clientIp);
                log.warn("IP {} bloqueado por exceder o limite de requisições para o endpoint sensível {}: {}", 
                        clientIp, path, pathCount);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"rate_limit_exceeded\",\"message\":\"Muitas requisições para este endpoint. Tente novamente mais tarde.\"}");
                return;
            }
        }
        
        // Continuar com a requisição
        filterChain.doFilter(request, response);
    }
    
    /**
     * Verifica se o endpoint é considerado sensível (mais susceptível a sobrecarga)
     * Endpoints sensíveis têm limites mais rigorosos de taxa de requisição
     */
    private boolean isSensitiveEndpoint(String path) {
        // Excluir URLs que já estão isentas de rate limiting
        for (String exemptPath : EXEMPT_PATHS) {
            if (path.startsWith(exemptPath)) {
                return false;
            }
        }
        
        // Considerar endpoints de autenticação e perfil como sensíveis
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/api/auth/") ||
               path.contains("/login") || 
               path.contains("/oauth2") ||
               path.contains("/profile") ||
               path.endsWith("/save") ||
               path.endsWith("/delete") ||
               path.endsWith("/update");
    }
    
    /**
     * Obtém o IP do cliente, considerando proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Se houver múltiplos IPs, pegar o primeiro (mais próximo do cliente)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * Incrementa o contador de requisições para um IP
     */
    private synchronized int incrementCounter(String ip) {
        int count = requestCountsByIp.getOrDefault(ip, 0) + 1;
        requestCountsByIp.put(ip, count);
        return count;
    }
    
    /**
     * Incrementa o contador de requisições para um IP e caminho específicos
     */
    private synchronized int incrementPathCounter(String ip, String path) {
        Map<String, Integer> pathCounts = requestCountsByIpAndPath.computeIfAbsent(ip, k -> new ConcurrentHashMap<>());
        int count = pathCounts.getOrDefault(path, 0) + 1;
        pathCounts.put(path, count);
        return count;
    }
    
    /**
     * Bloqueia um IP por um período de tempo configurável
     */
    private void blockIp(String ip) {
        blockedIps.put(ip, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(BLOCK_TIME_MINUTES));
    }
    
    /**
     * Verifica se um IP está bloqueado
     */
    private boolean isBlocked(String ip) {
        Long blockedUntil = blockedIps.get(ip);
        return blockedUntil != null && blockedUntil > System.currentTimeMillis();
    }
    
    /**
     * Reseta os contadores de requisições
     */
    private void resetCounters() {
        requestCountsByIp.clear();
        requestCountsByIpAndPath.clear();
        log.debug("Contadores de requisições resetados");
    }
    
    /**
     * Remove IPs bloqueados que já passaram do tempo de bloqueio
     */
    private void cleanBlockedIps() {
        long currentTime = System.currentTimeMillis();
        blockedIps.entrySet().removeIf(entry -> entry.getValue() < currentTime);
        log.debug("Limpeza de IPs bloqueados concluída: {} IPs ainda bloqueados", blockedIps.size());
    }
    
    /**
     * Verifica se o caminho está relacionado a autenticação ou autorização
     */
    private boolean isAuthRelatedPath(String path) {
        return path.contains("/auth") || 
               path.contains("/login") || 
               path.contains("/oauth2") || 
               path.equals("/api/user") ||
               path.equals("/api/api/user");
    }
    
    /**
     * Registra informações detalhadas sobre a sessão para diagnóstico
     */
    private void logSessionDiagnostics(HttpServletRequest request, String path, Authentication auth, HttpSession session) {
        if (auth == null) {
            log.debug("🔍 [Diagnóstico] Path {}: Nenhuma autenticação no contexto de segurança", path);
        } else {
            log.debug("🔍 [Diagnóstico] Path {}: Auth: {}, Auth Class: {}, Principal: {}, Authenticated: {}", 
                    path, auth.getName(), auth.getClass().getSimpleName(),
                    auth.getPrincipal() instanceof String ? auth.getPrincipal() : auth.getPrincipal().getClass().getSimpleName(),
                    auth.isAuthenticated());
        }
        
        if (session == null) {
            log.debug("🔍 [Diagnóstico] Path {}: Sessão não existe ou expirada", path);
            
            // Verificar cookies para diagnóstico
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                boolean hasJsessionId = false;
                for (Cookie cookie : cookies) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        hasJsessionId = true;
                        log.debug("🔍 [Diagnóstico] Cookie JSESSIONID encontrado mas sessão inválida. Domain: {}, Path: {}, MaxAge: {}", 
                                cookie.getDomain(), cookie.getPath(), cookie.getMaxAge());
                    }
                }
                if (!hasJsessionId) {
                    log.debug("🔍 [Diagnóstico] Cookie JSESSIONID não encontrado");
                }
            } else {
                log.debug("🔍 [Diagnóstico] Nenhum cookie encontrado na requisição");
            }
        } else {
            log.debug("🔍 [Diagnóstico] Path {}: Sessão ativa: ID={}, Criação={}, ÚltimoAcesso={}, MaxInactive={}s", 
                    path, session.getId(), 
                    new java.util.Date(session.getCreationTime()),
                    new java.util.Date(session.getLastAccessedTime()),
                    session.getMaxInactiveInterval());
        }
    }
}

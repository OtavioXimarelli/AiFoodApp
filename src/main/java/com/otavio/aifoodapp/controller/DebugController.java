package com.otavio.aifoodapp.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otavio.aifoodapp.util.DebugInfoExtractor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador consolidado para diagnóstico e depuração
 * Combina funcionalidades de debug de autenticação, sessão e cookies
 * ATENÇÃO: Este controlador só é ativo quando app.debug.enabled=true
 */
@RestController
@RequestMapping("/api/debug")
@ConditionalOnProperty(name = "app.debug.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class DebugController {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Endpoint principal para depuração - informações completas
     */
    @GetMapping("/info")
    public Map<String, Object> getCompleteDebugInfo(HttpServletRequest request) {
        log.info("Complete debug info requested by {}", request.getRemoteAddr());
        
        Map<String, Object> debugInfo = DebugInfoExtractor.extractCompleteDebugInfo(request);
        debugInfo.put("frontendUrl", frontendUrl);
        
        return debugInfo;
    }

    /**
     * Endpoint específico para informações de autenticação (compatibilidade)
     */
    @GetMapping("/auth-info")
    public Map<String, Object> getAuthInfo(HttpServletRequest request, HttpServletResponse response) {
        log.info("Authentication debug info requested by {}", request.getRemoteAddr());
        
        Map<String, Object> info = new HashMap<>();
        
        // Informações básicas da requisição
        info.putAll(DebugInfoExtractor.extractBasicRequestInfo(request));
        info.put("frontendUrl", frontendUrl);
        
        // Headers completos (para compatibilidade com endpoint original)
        info.put("headers", DebugInfoExtractor.extractHeaders(request));
        
        // Informações de sessão, autenticação e cookies
        info.put("session", DebugInfoExtractor.extractSessionInfo(request));
        info.put("authentication", DebugInfoExtractor.extractAuthenticationInfo());
        info.put("cookies", DebugInfoExtractor.extractCookieInfo(request));
        
        // Adicionar cookie de teste para verificar comportamento
        addTestCookie(response);
        
        return info;
    }

    /**
     * Endpoint específico para informações de sessão (compatibilidade)
     */
    @GetMapping("/session")
    public Map<String, Object> getSessionInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        
        // Informações de sessão
        Map<String, Object> sessionInfo = DebugInfoExtractor.extractSessionInfo(request);
        info.putAll(sessionInfo);
        
        // Informações de autenticação
        info.put("authentication", DebugInfoExtractor.extractAuthenticationInfo());
        
        // Informações de cookies
        info.put("cookies", DebugInfoExtractor.extractCookieInfo(request));
        
        return info;
    }

    /**
     * Endpoints para gerenciamento de sessão
     */
    @GetMapping("/sessions/info")
    public ResponseEntity<Map<String, Object>> getDetailedSessionInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        
        // Informações de sessão
        Map<String, Object> sessionInfo = DebugInfoExtractor.extractSessionInfo(request);
        info.putAll(sessionInfo);
        
        // Informações de autenticação
        info.put("authentication", DebugInfoExtractor.extractAuthenticationInfo());
        
        // Informações de cookies
        info.put("cookies", DebugInfoExtractor.extractCookieInfo(request));
        
        // Headers da requisição
        info.put("headers", DebugInfoExtractor.extractHeaders(request));
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/sessions/create")
    public ResponseEntity<Map<String, Object>> createSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        session.setAttribute("testAttribute", "Created at " + System.currentTimeMillis());
        
        // Adicionar cookie de teste
        Cookie cookie = new Cookie("TEST_SESSION_COOKIE", "test-value");
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Session created and test cookie set",
            "sessionId", session.getId(),
            "creationTime", new java.util.Date(session.getCreationTime()).toString()
        ));
    }

    @GetMapping("/sessions/invalidate")
    public ResponseEntity<Map<String, Object>> invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String sessionId = session.getId();
            session.invalidate();
            
            // Remover o cookie JSESSIONID
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Session invalidated",
                "invalidatedSessionId", sessionId
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "No active session to invalidate"
            ));
        }
    }

    /**
     * Endpoint para testar cookies
     */
    @GetMapping("/test-cookie")
    public Map<String, Object> testCookie(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        // Verificar cookies existentes
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            result.put("existingCookies", Arrays.stream(cookies)
                    .map(Cookie::getName)
                    .collect(Collectors.toList()));
        } else {
            result.put("existingCookies", "No cookies found");
        }
        
        // Adicionar novos cookies de teste
        for (int i = 1; i <= 3; i++) {
            Cookie testCookie = new Cookie("test_cookie_" + i, "value_" + i);
            testCookie.setPath("/");
            testCookie.setHttpOnly(true);
            testCookie.setSecure(true);
            testCookie.setMaxAge(60 * i);
            
            if (i == 2) {
                // Para o cookie 2, tentar definir explicitamente o atributo SameSite
                String cookieHeader = String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=Lax", 
                        testCookie.getName(), testCookie.getValue(), testCookie.getPath(), testCookie.getMaxAge());
                response.addHeader("Set-Cookie", cookieHeader);
            } else {
                response.addCookie(testCookie);
            }
        }
        
        result.put("newCookies", Arrays.asList("test_cookie_1", "test_cookie_2", "test_cookie_3"));
        result.put("message", "Test cookies added. Please check if they are visible in your browser.");
        
        return result;
    }

    /**
     * Método auxiliar para adicionar cookie de teste
     */
    private void addTestCookie(HttpServletResponse response) {
        Cookie testCookie = new Cookie("debug_test_cookie", "test_value");
        testCookie.setPath("/");
        testCookie.setHttpOnly(true);
        testCookie.setSecure(true);
        testCookie.setMaxAge(60);
        
        String domain = System.getenv("COOKIE_DOMAIN");
        if (domain != null && !domain.isEmpty()) {
            testCookie.setDomain(domain);
        }
        
        response.addCookie(testCookie);
        response.addHeader("X-Debug-Test", "test_value");
    }
}

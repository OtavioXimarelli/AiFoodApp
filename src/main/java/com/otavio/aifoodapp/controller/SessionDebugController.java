package com.otavio.aifoodapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para diagnóstico de problemas com sessões e cookies
 */
@RestController
@RequestMapping("/api/debug/sessions")
@Slf4j
public class SessionDebugController {

    // frontendUrl removido pois não era utilizado
    
    /**
     * Exibe informações sobre a sessão atual
     */
    @GetMapping("/info")
    public ResponseEntity<?> getSessionInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            info.put("sessionExists", true);
            info.put("sessionId", session.getId());
            info.put("creationTime", new java.util.Date(session.getCreationTime()).toString());
            info.put("lastAccessedTime", new java.util.Date(session.getLastAccessedTime()).toString());
            info.put("maxInactiveInterval", session.getMaxInactiveInterval() + " seconds");
            info.put("isNew", session.isNew());
            
            // Listar atributos sem expor valores sensíveis
            Map<String, Boolean> attributes = new HashMap<>();
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                attributes.put(name, true);
            }
            info.put("attributes", attributes);
            
        } else {
            info.put("sessionExists", false);
        }
        
        // Informações de autenticação
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> authInfo = new HashMap<>();
        if (auth != null) {
            authInfo.put("authenticated", auth.isAuthenticated());
            authInfo.put("principal", auth.getPrincipal() instanceof String ? auth.getPrincipal() : auth.getPrincipal().getClass().getSimpleName());
            authInfo.put("name", auth.getName());
            authInfo.put("authorities", auth.getAuthorities().toString());
        } else {
            authInfo.put("authenticated", false);
        }
        info.put("authentication", authInfo);
        
        // Informações sobre cookies
        Cookie[] cookies = request.getCookies();
        Map<String, Object> cookieInfo = new HashMap<>();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                Map<String, Object> cookieDetails = new HashMap<>();
                cookieDetails.put("domain", cookie.getDomain() != null ? cookie.getDomain() : "default");
                cookieDetails.put("path", cookie.getPath());
                cookieDetails.put("maxAge", cookie.getMaxAge());
                cookieDetails.put("secure", cookie.getSecure());
                cookieDetails.put("httpOnly", cookie.isHttpOnly());
                cookieInfo.put(cookie.getName(), cookieDetails);
            }
        }
        info.put("cookies", cookieInfo);
        
        // Headers da requisição
        Map<String, String> headerInfo = new HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headerInfo.put(name, request.getHeader(name));
        }
        info.put("headers", headerInfo);
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Cria uma nova sessão e define um cookie para teste
     */
    @GetMapping("/create")
    public ResponseEntity<?> createSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        session.setAttribute("testAttribute", "Created at " + System.currentTimeMillis());
        
        // Definir um cookie de teste
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
    
    /**
     * Invalida a sessão atual
     */
    @GetMapping("/invalidate")
    public ResponseEntity<?> invalidateSession(HttpServletRequest request, HttpServletResponse response) {
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
}

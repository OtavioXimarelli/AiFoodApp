package com.otavio.aifoodapp.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Utilitário para extrair informações de debug de forma consistente
 * Centraliza lógicas comuns dos controladores de debug
 */
public final class DebugInfoExtractor {

    private DebugInfoExtractor() {
        // Utility class
    }

    /**
     * Extrai informações básicas da requisição HTTP
     */
    public static Map<String, Object> extractBasicRequestInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        info.put("remoteAddress", request.getRemoteAddr());
        info.put("remoteHost", request.getRemoteHost());
        info.put("serverName", request.getServerName());
        info.put("serverPort", request.getServerPort());
        info.put("scheme", request.getScheme());
        info.put("requestURL", request.getRequestURL().toString());
        info.put("method", request.getMethod());
        info.put("path", request.getServletPath());
        return info;
    }

    /**
     * Extrai informações da sessão HTTP
     */
    public static Map<String, Object> extractSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> sessionInfo = new HashMap<>();
        
        sessionInfo.put("hasActiveSession", session != null);
        
        if (session != null) {
            sessionInfo.put("sessionId", session.getId());
            sessionInfo.put("creationTime", session.getCreationTime());
            sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
            sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
            sessionInfo.put("isNew", session.isNew());
            
            // Atributos da sessão (mascarados por segurança)
            Map<String, String> attributes = new HashMap<>();
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                attributes.put(name, "[MASKED]");
            }
            sessionInfo.put("attributes", attributes);
        }
        
        return sessionInfo;
    }

    /**
     * Extrai informações de autenticação
     */
    public static Map<String, Object> extractAuthenticationInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> authInfo = new HashMap<>();
        
        if (auth != null) {
            authInfo.put("name", auth.getName());
            authInfo.put("authenticated", auth.isAuthenticated());
            authInfo.put("authType", auth.getClass().getSimpleName());
            authInfo.put("authorities", auth.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));
            
            // Principal info (mascarado se for objeto complexo)
            Object principal = auth.getPrincipal();
            if (principal instanceof String) {
                authInfo.put("principal", principal);
            } else if (principal != null) {
                authInfo.put("principal", "[" + principal.getClass().getSimpleName() + "]");
            } else {
                authInfo.put("principal", null);
            }
            
            // Informações específicas do OAuth2
            if (auth instanceof OAuth2AuthenticationToken oauth2Auth) {
                Map<String, Object> oauth2Info = new HashMap<>();
                oauth2Info.put("registrationId", oauth2Auth.getAuthorizedClientRegistrationId());
                
                OAuth2User oauth2User = oauth2Auth.getPrincipal();
                Map<String, Object> userAttributes = new HashMap<>();
                for (String key : oauth2User.getAttributes().keySet()) {
                    if ("email".equalsIgnoreCase(key) || "name".equalsIgnoreCase(key)) {
                        userAttributes.put(key, oauth2User.getAttribute(key));
                    } else {
                        userAttributes.put(key, "[MASKED]");
                    }
                }
                oauth2Info.put("userAttributes", userAttributes);
                authInfo.put("oauth2", oauth2Info);
            }
        } else {
            authInfo.put("authenticated", false);
            authInfo.put("name", null);
        }
        
        return authInfo;
    }

    /**
     * Extrai informações dos cookies (mascarados por segurança)
     */
    public static Map<String, Object> extractCookieInfo(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, Object> cookieInfo = new HashMap<>();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                Map<String, Object> details = new HashMap<>();
                details.put("value", "[MASKED]"); // Sempre mascarar valores
                details.put("domain", cookie.getDomain() != null ? cookie.getDomain() : "default");
                details.put("path", cookie.getPath());
                details.put("maxAge", cookie.getMaxAge());
                details.put("secure", cookie.getSecure());
                details.put("httpOnly", cookie.isHttpOnly());
                cookieInfo.put(cookie.getName(), details);
            }
        }
        
        return cookieInfo;
    }

    /**
     * Extrai headers HTTP importantes
     */
    public static Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    /**
     * Extrai apenas headers importantes para debug
     */
    public static Map<String, String> extractImportantHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        String[] importantHeaders = {"User-Agent", "Referer", "Origin", "Accept", "Authorization"};
        
        for (String headerName : importantHeaders) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                // Mascarar Authorization header por segurança
                if ("Authorization".equalsIgnoreCase(headerName)) {
                    headers.put(headerName, "[MASKED]");
                } else {
                    headers.put(headerName, headerValue);
                }
            }
        }
        
        return headers;
    }

    /**
     * Cria um resumo completo das informações de debug
     */
    public static Map<String, Object> extractCompleteDebugInfo(HttpServletRequest request) {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("request", extractBasicRequestInfo(request));
        debugInfo.put("session", extractSessionInfo(request));
        debugInfo.put("authentication", extractAuthenticationInfo());
        debugInfo.put("cookies", extractCookieInfo(request));
        debugInfo.put("headers", extractImportantHeaders(request));
        return debugInfo;
    }
}
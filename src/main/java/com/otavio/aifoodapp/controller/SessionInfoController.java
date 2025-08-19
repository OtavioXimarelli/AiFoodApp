package com.otavio.aifoodapp.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para diagnosticar informações de sessão
 * Útil apenas em ambientes de desenvolvimento para depuração
 */
@RestController
@RequestMapping("/api/debug")
@Slf4j
public class SessionInfoController {

    /**
     * Endpoint para visualizar informações da sessão atual
     * @param request HTTP request com sessão
     * @return Mapa com informações da sessão
     */
    @GetMapping("/session")
    public Map<String, Object> getSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> info = new HashMap<>();
        
        // Info básica
        info.put("hasActiveSession", session != null);
        
        if (session != null) {
            info.put("sessionId", session.getId());
            info.put("creationTime", new java.util.Date(session.getCreationTime()).toString());
            info.put("lastAccessedTime", new java.util.Date(session.getLastAccessedTime()).toString());
            info.put("maxInactiveInterval", session.getMaxInactiveInterval() + " seconds");
            info.put("isNew", session.isNew());
            
            // Atributos da sessão (não expor valores sensíveis)
            Map<String, String> attributes = new HashMap<>();
            Collections.list(session.getAttributeNames()).forEach(name -> 
                attributes.put(name, "[PROTECTED]")
            );
            info.put("attributes", attributes);
        }
        
        // Informações de autenticação
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("isAuthenticated", auth.isAuthenticated());
            authInfo.put("principal", auth.getPrincipal() instanceof String ? auth.getPrincipal() : "[OBJECT]");
            authInfo.put("name", auth.getName());
            authInfo.put("authorities", auth.getAuthorities().toString());
            
            // Informações específicas do OAuth2
            if (auth instanceof OAuth2AuthenticationToken) {
                authInfo.put("type", "OAuth2");
                authInfo.put("clientRegistrationId", ((OAuth2AuthenticationToken) auth).getAuthorizedClientRegistrationId());
            }
            
            info.put("authentication", authInfo);
        } else {
            info.put("authentication", null);
        }
        
        // Cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Map<String, Object> cookieInfo = new HashMap<>();
            for (Cookie cookie : cookies) {
                Map<String, Object> details = new HashMap<>();
                details.put("path", cookie.getPath());
                details.put("domain", cookie.getDomain() != null ? cookie.getDomain() : "default");
                details.put("maxAge", cookie.getMaxAge());
                details.put("secure", cookie.getSecure());
                details.put("httpOnly", cookie.isHttpOnly());
                
                // Não exibir valores de cookies para segurança
                cookieInfo.put(cookie.getName(), details);
            }
            info.put("cookies", cookieInfo);
        }
        
        return info;
    }
}

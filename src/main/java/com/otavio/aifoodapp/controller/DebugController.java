package com.otavio.aifoodapp.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para fins de depuração de autenticação
 * ATENÇÃO: Este controlador deve ser desativado ou protegido em ambientes de produção
 */
@RestController
@RequestMapping("/api/debug")
@Slf4j
public class DebugController {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Endpoint para depurar informações de autenticação
     */
    @GetMapping("/auth-info")
    public Map<String, Object> getAuthInfo(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> info = new HashMap<>();
        
        // Informações básicas
        info.put("remoteAddress", request.getRemoteAddr());
        info.put("remoteHost", request.getRemoteHost());
        info.put("serverName", request.getServerName());
        info.put("serverPort", request.getServerPort());
        info.put("scheme", request.getScheme());
        info.put("requestURL", request.getRequestURL().toString());
        info.put("frontendUrl", frontendUrl);

        // Headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        info.put("headers", headers);

        // Cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Map<String, Map<String, Object>> cookieDetails = Arrays.stream(cookies)
                    .collect(Collectors.toMap(
                            Cookie::getName,
                            cookie -> {
                                Map<String, Object> details = new HashMap<>();
                                details.put("value", "[MASKED]"); // Não exibir valores por segurança
                                details.put("domain", cookie.getDomain() != null ? cookie.getDomain() : "null");
                                details.put("path", cookie.getPath());
                                details.put("maxAge", cookie.getMaxAge());
                                details.put("secure", cookie.getSecure());
                                details.put("httpOnly", cookie.isHttpOnly());
                                return details;
                            }
                    ));
            info.put("cookies", cookieDetails);
        } else {
            info.put("cookies", "No cookies found");
        }

        // Informações de Sessão
        HttpSession session = request.getSession(false);
        if (session != null) {
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("id", session.getId());
            sessionInfo.put("creationTime", session.getCreationTime());
            sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
            sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
            sessionInfo.put("isNew", session.isNew());

            // Atributos da sessão (apenas nomes, sem valores por segurança)
            Enumeration<String> attributeNames = session.getAttributeNames();
            Map<String, String> attributes = new HashMap<>();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                attributes.put(name, "[MASKED]");
            }
            sessionInfo.put("attributes", attributes);
            
            info.put("session", sessionInfo);
        } else {
            info.put("session", "No active session");
        }

        // Informações de Autenticação
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("name", auth.getName());
            authInfo.put("authenticated", auth.isAuthenticated());
            authInfo.put("principal", auth.getPrincipal().toString());
            authInfo.put("authorities", auth.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));
            authInfo.put("authType", auth.getClass().getSimpleName());
            
            // Informações específicas do OAuth2
            if (auth instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) auth;
                Map<String, Object> oauth2Info = new HashMap<>();
                oauth2Info.put("registrationId", oauth2Auth.getAuthorizedClientRegistrationId());
                
                OAuth2User oauth2User = oauth2Auth.getPrincipal();
                Map<String, Object> userAttributes = new HashMap<>();
                for (String key : oauth2User.getAttributes().keySet()) {
                    if (key.equalsIgnoreCase("email") || key.equalsIgnoreCase("name")) {
                        userAttributes.put(key, oauth2User.getAttribute(key));
                    } else {
                        userAttributes.put(key, "[MASKED]"); // Não exibir valores sensíveis
                    }
                }
                oauth2Info.put("userAttributes", userAttributes);
                
                authInfo.put("oauth2", oauth2Info);
            }
            
            info.put("authentication", authInfo);
        } else {
            info.put("authentication", "Not authenticated");
        }

        // Adicionar um cookie de teste para verificar o comportamento
        Cookie testCookie = new Cookie("debug_test_cookie", "test_value");
        testCookie.setPath("/");
        testCookie.setHttpOnly(true);
        testCookie.setSecure(true);
        testCookie.setMaxAge(60); // 1 minuto apenas
        
        String domain = System.getenv("COOKIE_DOMAIN");
        if (domain != null && !domain.isEmpty()) {
            testCookie.setDomain(domain);
        }
        
        response.addCookie(testCookie);
        
        // Adicionar um header de teste para verificar comportamento
        response.addHeader("X-Debug-Test", "test_value");
        
        log.info("Debug authentication info requested by {}", request.getRemoteAddr());
        
        return info;
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
            testCookie.setMaxAge(60 * i); // Cada cookie com tempo de expiração diferente
            
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
}

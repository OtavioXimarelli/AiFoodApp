package com.otavio.aifoodapp.security;

import java.util.Base64;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Repository para armazenar a requisição de autorização OAuth2 em cookies,
 * o que proporciona maior robustez do que o armazenamento padrão em sessão.
 */
@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3 minutos

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.debug("Carregando requisição de autorização OAuth2 dos cookies");
        return getCookieValueAsObject(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
                                        HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.debug("Removendo cookies de requisição de autorização OAuth2");
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        log.debug("Salvando requisição de autorização OAuth2 em cookies");
        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serializeObject(authorizationRequest),
                COOKIE_EXPIRE_SECONDS);
        
        String redirectUriAfterLogin = request.getParameter("redirect_uri");
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isEmpty()) {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = this.loadAuthorizationRequest(request);
        if (authRequest != null) {
            log.debug("Removendo requisição de autorização OAuth2 dos cookies");
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
        }
        return authRequest;
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(true); // Sempre usar Secure em produção
        
        // O atributo SameSite será adicionado pelo SameSiteCookieFilter
        // mas também podemos adicioná-lo diretamente aqui via header
        String cookieValue = String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=Lax", 
                name, value, cookie.getPath(), cookie.getMaxAge());
        
        if (System.getenv("COOKIE_DOMAIN") != null) {
            String domain = System.getenv("COOKIE_DOMAIN");
            cookieValue += "; Domain=" + domain;
            cookie.setDomain(domain);
        }
        
        response.addHeader("Set-Cookie", cookieValue);
        response.addCookie(cookie); // Adicionamos o cookie também pelo método padrão
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    cookie.setSecure(true);
                    
                    // Adicionar também via header para garantir que os atributos SameSite sejam incluídos
                    String cookieValue = String.format("%s=; Path=%s; Max-Age=0; HttpOnly; Secure; SameSite=Lax", 
                            name, cookie.getPath());
                    
                    if (System.getenv("COOKIE_DOMAIN") != null) {
                        String domain = System.getenv("COOKIE_DOMAIN");
                        cookieValue += "; Domain=" + domain;
                        cookie.setDomain(domain);
                    }
                    
                    response.addHeader("Set-Cookie", cookieValue);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
    }

    private <T> T getCookieValueAsObject(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return deserializeObject(cookie.getValue());
                }
            }
        }
        return null;
    }

    private String serializeObject(Object object) {
        try {
            // Usando ByteArrayOutputStream e ObjectOutputStream com try-with-resources
            java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
            try (java.io.ObjectOutputStream objectStream = new java.io.ObjectOutputStream(byteStream)) {
                objectStream.writeObject(object);
                objectStream.flush();
                return Base64.getUrlEncoder().encodeToString(byteStream.toByteArray());
            }
        } catch (java.io.IOException e) {
            log.error("Erro ao serializar objeto para cookie: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeObject(String value) {
        try {
            // Usando ByteArrayInputStream e ObjectInputStream com try-with-resources
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            java.io.ByteArrayInputStream byteStream = new java.io.ByteArrayInputStream(bytes);
            try (java.io.ObjectInputStream objectStream = new java.io.ObjectInputStream(byteStream)) {
                Object object = objectStream.readObject();
                return (T) object;
            }
        } catch (java.io.IOException e) {
            log.error("Erro ao deserializar objeto do cookie (IO): {}", e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            log.error("Erro ao deserializar objeto do cookie (Classe não encontrada): {}", e.getMessage());
            return null;
        } catch (ClassCastException e) {
            log.error("Erro ao deserializar objeto do cookie (Cast inválido): {}", e.getMessage());
            return null;
        }
    }
}

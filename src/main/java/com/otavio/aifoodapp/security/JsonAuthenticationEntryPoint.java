package com.otavio.aifoodapp.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Classe personalizada para tratar erros de autenticação e retornar respostas JSON
 * em vez de redirecionar para a página de login
 */
@Component
@Slf4j
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        log.debug("Handling authentication exception: {}", authException.getMessage());
        
        // Determinar o tipo de resposta com base no caminho e cabeçalhos
        String path = request.getRequestURI();
        String accept = request.getHeader("Accept");
        
        // Se for uma requisição AJAX/API, retornar JSON
        boolean isApiRequest = path.startsWith("/api/") || 
                              (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
        
        if (isApiRequest) {
            log.debug("Returning JSON error response for API request");
            
            // Configurar resposta JSON
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "Unauthorized");
            body.put("message", "Authentication required");
            body.put("path", request.getRequestURI());
            
            try (PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(body));
                writer.flush();
            }
        } else {
            // Para requisições de páginas web, redirecionar para login
            log.debug("Redirecting to login page for web request");
            response.sendRedirect("/login");
        }
    }
}

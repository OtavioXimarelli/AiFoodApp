package com.otavio.aifoodapp.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para lidar com erros na aplicação de forma mais amigável
 * Especialmente útil para erros durante o fluxo OAuth2
 */
@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        // Get error details
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        String errorMsg = errorMessage != null ? errorMessage.toString() : "Unknown error";
        String path = requestUri != null ? requestUri.toString() : request.getRequestURI();
        
        log.error("Error occurred: {} - {}, Path: {}", statusCode, errorMsg, path);
        
        // Verificar se a requisição está relacionada ao OAuth2
        if (path != null && 
            (path.contains("/oauth2/") || 
             path.contains("/login/oauth2/") || 
             path.contains("error=true"))) {
            
            log.warn("OAuth2 related error detected: {} - {}. Redirecting to frontend.", statusCode, errorMsg);
            
            // Tratar erro específico de "authorization_request_not_found"
            if (errorMsg != null && errorMsg.contains("authorization_request_not_found")) {
                log.info("Detectado erro de 'authorization_request_not_found', problema comum com cookies de estado OAuth2");
                // Redirecionar para a página de login com mensagem específica
                return new RedirectView(frontendUrl + 
                    "/login?error=oauth_state_missing&message=Erro na autenticação. Por favor, tente novamente.");
            }
            
            // Se for um erro de autorização (403) ou autenticação (401)
            if (statusCode == 401 || statusCode == 403) {
                return new RedirectView(frontendUrl + 
                    "/login?error=auth_failed&message=Login não autorizado. Por favor, verifique suas credenciais.");
            }
            
            // Para erros de OAuth2 gerais, redirecionar para o frontend com informações de erro
            return new RedirectView(frontendUrl + "/login?error=true&code=" + statusCode + "&message=" + errorMsg);
        }
        
        // For API requests, return JSON
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", statusCode);
            body.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
            body.put("message", errorMsg);
            body.put("path", path);
            
            if (exception != null) {
                // Add stack trace for debugging in development
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ((Throwable) exception).printStackTrace(pw);
                body.put("trace", sw.toString());
            }
            
            return ResponseEntity.status(statusCode).body(body);
        }
        
        // For web requests, redirect to frontend with error info
        return new RedirectView(frontendUrl + "/error?code=" + statusCode + "&message=" + errorMsg);
    }
    
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String path = request.getServletPath();
        
        return path.startsWith("/api/") || 
               (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
    }
}

package com.otavio.aifoodapp.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler personalizado para sucesso de autenticação.
 * Isso é usado para adicionar cabeçalhos personalizados e cookies após autenticação bem-sucedida.
 */
@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Método chamado quando a autenticação é bem-sucedida.
     * 
     * @param request O request HTTP
     * @param response O response HTTP
     * @param authentication O objeto de autenticação
     * @throws IOException Em caso de erro de I/O
     * @throws ServletException Em caso de erro de servlet
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("Autenticação bem-sucedida para usuário: {}", authentication.getName());
        
        // Adicionar cabeçalho personalizado
        response.addHeader("X-Auth-Success", "true");
        
        // Extrair informações do usuário para log e depuração
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            OAuth2User oauth2User = oauth2Auth.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            log.info("Usuário OAuth2 autenticado: {} ({})", name, email);
        }
        
        // Adicionar cabeçalho com frontendUrl para depuração
        response.addHeader("X-Frontend-URL", frontendUrl);
        
        // Se não houvesse outro handler, poderíamos redirecionar para a página inicial
        // Mas como já temos o OAuth2LoginSuccessHandler, deixamos esse comportamento para ele
        // Se for necessário redirecionar, descomentar a linha abaixo
        // response.sendRedirect(frontendUrl);
    }
}

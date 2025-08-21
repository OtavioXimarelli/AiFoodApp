package com.otavio.aifoodapp.security;

import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 login bem-sucedido. Processando autenticação...");

        if (!(authentication.getPrincipal() instanceof OAuth2User)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        // Busca o usuário no banco de dados pelo email fornecido pelo Google
        User user = (User) userRepository.findByLogin(email);
        if (user == null) {
            throw new IllegalStateException("Usuário OAuth2 não encontrado no banco de dados: " + email);
        }

        // Não gere token JWT próprio, apenas redirecione para o frontend após login bem-sucedido
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login/callback")
                .build().toUriString();

        log.info("Redirecionando para o frontend: {}", targetUrl);

        // Limpa os atributos da sessão que foram usados durante o processo de autenticação
        clearAuthenticationAttributes(request);

        // Executa o redirecionamento
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

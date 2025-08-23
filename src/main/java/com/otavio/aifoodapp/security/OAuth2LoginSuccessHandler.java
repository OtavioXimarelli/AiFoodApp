package com.otavio.aifoodapp.security;

import com.otavio.aifoodapp.enums.UserRoles;
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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 login bem-sucedido. Processando usuário...");

        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        String email = oauth2User.getAttribute("email");
        if (email == null) {
            log.error("Não foi possível obter o e-mail do provedor OAuth2. Atributos: {}", oauth2User.getAttributes());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?error=true&message=email_not_found");
            return;
        }

        // ✅ LÓGICA DE "ENCONTRAR OU CRIAR"
        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateUser(existingUser, oauth2User)) // Se encontrar, atualiza
                .orElseGet(() -> createUser(oauth2User));                   // Se não, cria um novo

        log.info("Usuário {} processado com sucesso.", user.getEmail());

        // Define a URL de redirecionamento e deixa o Spring cuidar do resto
        String targetUrl = frontendUrl + "/dashboard"; // Ou a página que preferir
        setDefaultTargetUrl(targetUrl);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * Atualiza as informações de um usuário existente com os dados mais recentes do OAuth2.
     */
    private User updateUser(User existingUser, OAuth2User oauth2User) {
        log.debug("Atualizando usuário existente: {}", existingUser.getEmail());
        existingUser.setFirstName(oauth2User.getAttribute("given_name"));
        existingUser.setLastName(oauth2User.getAttribute("family_name"));
        existingUser.setProfilePicture(oauth2User.getAttribute("picture"));
        existingUser.setLastLoginAt(OffsetDateTime.now());
        return userRepository.save(existingUser);
    }

    /**
     * Cria um novo usuário no banco de dados com as informações do OAuth2.
     */
    private User createUser(OAuth2User oauth2User) {
        // ✅ CORREÇÃO: Usando concatenação de string para evitar ambiguidade no compilador.
        log.warn("Criando novo usuário para o e-mail: " + oauth2User.getAttribute("email"));
        User newUser = new User();
        newUser.setEmail(oauth2User.getAttribute("email"));
        newUser.setGoogleId(oauth2User.getAttribute("sub"));
        newUser.setFirstName(oauth2User.getAttribute("given_name"));
        newUser.setLastName(oauth2User.getAttribute("family_name"));
        newUser.setProfilePicture(oauth2User.getAttribute("picture"));
        newUser.setRole(UserRoles.USER); // Define um papel padrão
        newUser.setProvider("GOOGLE");
        newUser.setIsActive(true);
        newUser.setCreatedAt(OffsetDateTime.now());
        newUser.setLastLoginAt(OffsetDateTime.now());
        return userRepository.save(newUser);
    }
}

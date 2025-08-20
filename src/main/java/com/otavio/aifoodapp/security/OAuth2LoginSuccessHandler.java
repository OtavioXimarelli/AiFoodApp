package com.otavio.aifoodapp.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom authentication success handler for OAuth2 login
 * Updates user info and last login time
 */
@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserRepository userRepository;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @PostConstruct
    public void init() {
        // Definindo a URL de redirecionamento padrão para o frontend com o dashboard
        this.setDefaultTargetUrl(frontendUrl + "/dashboard");
        log.info("OAuth2 success handler configured with redirect URL: {}", frontendUrl + "/dashboard");
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String email = oauth2User.getAttribute("email");
            
            log.debug("OAuth2 authentication success for email: {}", email);
            
            if (email != null) {
                // Try to find the user by email
                Optional<User> userOptional = userRepository.findByEmail(email);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    updateUserInfo(user, oauth2User);
                    log.info("Updated existing user info for: {}", email);
                } else {
                    // Create a new user if not found
                    createNewUser(oauth2User);
                    log.info("Created new user from OAuth2 login: {}", email);
                }
                
                // Garantir que a sessão seja criada e persistente
                HttpSession session = request.getSession(true);
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                session.setAttribute("USER_EMAIL", email);
                
                // Configurar tempo de expiração da sessão
                session.setMaxInactiveInterval(60 * 60 * 24 * 30); // 30 dias
            }
        }
        
        // Validar authentication para evitar NullPointerException
        if (authentication == null) {
            log.error("Authentication object is null during redirect");
            try {
                response.sendRedirect(frontendUrl + "/login?error=invalid_auth");
            } catch (IOException e) {
                log.error("Failed to redirect with null authentication: {}", e.getMessage(), e);
            }
            return;
        }

        try {
            // Log session information for debugging
            log.info("Session ID before redirect: {}", request.getSession().getId());
            log.info("Redirecting to target URL: {}", this.getDefaultTargetUrl());
            // Já validamos authentication == null acima, então é seguro chamar getName()
            log.info("Authentication name: {}", authentication.getName());
            log.info("Frontend URL configured as: {}", frontendUrl);
            
            // Ensure we're not causing a redirect loop
            String referer = request.getHeader("Referer");
            log.info("Referer: {}", referer);
            
            // Em vez de usar o super, fazer redirecionamento direto para o frontend
            String redirectUrl = frontendUrl + "/dashboard";
            log.info("Redirecting directly to frontend: {}", redirectUrl);
            
            try {
                // Primeiro vamos garantir que a resposta ainda está aberta
                if (response.isCommitted()) {
                    log.warn("Response already committed, can't redirect to {}", redirectUrl);
                    return;
                }
                
                // Limpar o buffer antes do redirecionamento para evitar erros
                response.resetBuffer();
                
                // Verificar e garantir que a sessão está configurada corretamente
                if (request.getSession(false) != null) {
                    log.debug("Session is valid before redirect: {}", request.getSession().getId());
                } else {
                    log.warn("No session available before redirect, creating new session");
                    request.getSession(true);
                }
                
                // Adicionar um header para identificar o sucesso do login
                response.setHeader("X-Auth-Success", "true");
                
                // Simplificar e usar apenas 302 redirect sem cookies complexos
                log.info("Performing 302 redirect to frontend: {}", redirectUrl);
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.setHeader("Location", redirectUrl);
                response.flushBuffer();
                
                log.info("Successfully redirected after OAuth2 login");
            } catch (IOException | IllegalStateException e) {
                log.error("Error during redirect: {}", e.getMessage(), e);
                // Tentar uma abordagem alternativa em caso de erro - JavaScript redirect
                try {
                    response.resetBuffer();
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write("<html><head><script>window.location.href='" + redirectUrl + 
                                          "';</script></head><body>Redirecting to dashboard...</body></html>");
                    response.flushBuffer();
                } catch (IOException | IllegalStateException ex) {
                    log.error("Failed even with JavaScript redirect: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during OAuth2 login success redirect: {}", e.getMessage(), e);
            // Fall back to simplest possible redirect approach
            try {
                String redirectUrl = frontendUrl + "/dashboard";
                log.info("Falling back to simplest redirect approach for: {}", redirectUrl);
                
                // Reset any previous response
                response.resetBuffer();
                
                // Set response headers directly
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.setHeader("Location", redirectUrl);
                response.flushBuffer();
            } catch (IOException | IllegalStateException ex) {
                log.error("Failed to redirect even in emergency fallback handler: {}", ex.getMessage(), ex);
                
                // Last resort - try to send plain text
                try {
                    response.setContentType("text/plain;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Authentication successful. Please navigate to " + frontendUrl + "/dashboard manually.");
                    response.flushBuffer();
                } catch (IOException ignored) {
                    // Nothing more we can do
                }
            }
        }
    }
    
    private void updateUserInfo(User user, OAuth2User oauth2User) {
        try {
            // Update user details from OAuth attributes
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            String name = (String) attributes.get("name");
            if (name != null && name.contains(" ")) {
                String[] nameParts = name.split(" ", 2);
                user.setFirstName(nameParts[0]);
                user.setLastName(nameParts[1]);
            } else {
                // Handle potential missing attributes safely
                String firstName = (String) attributes.get("given_name");
                String lastName = (String) attributes.get("family_name");
                
                // Use fallbacks if attributes are missing
                user.setFirstName(firstName != null ? firstName : (name != null ? name : "User"));
                user.setLastName(lastName != null ? lastName : "");
            }
            
            // Set Google-specific fields with null checks
            user.setGoogleId((String) attributes.get("sub"));
            user.setProfilePicture((String) attributes.get("picture"));
            user.setProvider("GOOGLE");
            
            // Ensure the role is set to avoid constraints
            if (user.getRole() == null) {
                user.setRole(com.otavio.aifoodapp.enums.UserRoles.USER);
            }
            
            user.setLastLoginAt(OffsetDateTime.now());
            
            userRepository.save(user);
            log.debug("Successfully updated user information for: {}", user.getEmail());
        } catch (Exception e) {
            // Log error but don't prevent login
            log.error("Error updating user information: {}", e.getMessage(), e);
        }
    }
    
    private User createNewUser(OAuth2User oauth2User) {
        try {
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            User newUser = new User();
            String email = (String) attributes.get("email");
            if (email == null) {
                log.error("Cannot create user - email is null in OAuth2 attributes");
                throw new IllegalArgumentException("Email is required");
            }
            
            newUser.setEmail(email);
            
            String name = (String) attributes.get("name");
            if (name != null && name.contains(" ")) {
                String[] nameParts = name.split(" ", 2);
                newUser.setFirstName(nameParts[0]);
                newUser.setLastName(nameParts[1]);
            } else {
                // Handle potential missing attributes safely
                String firstName = (String) attributes.get("given_name");
                String lastName = (String) attributes.get("family_name");
                
                // Use fallbacks if attributes are missing
                newUser.setFirstName(firstName != null ? firstName : (name != null ? name : "User"));
                newUser.setLastName(lastName != null ? lastName : "");
            }
            
            // Set Google-specific fields with null checks
            newUser.setGoogleId((String) attributes.get("sub"));
            newUser.setProfilePicture((String) attributes.get("picture"));
            newUser.setProvider("GOOGLE");
            newUser.setIsActive(true);
            
            // Ensure the role is set to avoid constraints
            newUser.setRole(com.otavio.aifoodapp.enums.UserRoles.USER);
            
            // Set timestamps
            newUser.setCreatedAt(OffsetDateTime.now());
            newUser.setLastLoginAt(OffsetDateTime.now());
            
            User savedUser = userRepository.save(newUser);
            log.info("Successfully created new user: {}", email);
            return savedUser;
        } catch (DataAccessException | IllegalArgumentException e) {
            log.error("Failed to create new user from OAuth2 data: {}", e.getMessage(), e);
            throw e; // Re-throw as this is critical
        }
    }
}

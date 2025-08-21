package com.otavio.aifoodapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otavio.aifoodapp.dto.UserDTO;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Standard OAuth2 Authentication Controller
 * Uses Spring Security OAuth2 without custom token handling
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get current authenticated user information
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            Optional<User> dbUser = (email != null) ? userRepository.findByEmail(email) : Optional.empty();
            if (dbUser.isPresent()) {
                return ResponseEntity.ok(UserDTO.fromUser(dbUser.get()));
            } else {
                UserDTO dto = new UserDTO(null, email, name, "USER");
                return ResponseEntity.ok(dto);
            }
        }

        if (authentication.getPrincipal() instanceof User user) {
            return ResponseEntity.ok(UserDTO.fromUser(user));
        }

        return ResponseEntity.status(401).build();
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        Map<String, String> result = new HashMap<>();
        result.put("message", "Logged out successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * Get Google OAuth2 login URL
     */
    @GetMapping("/login/google")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", "/oauth2/authorization/google");
        response.put("message", "Redirect to this URL to login with Google");
        return ResponseEntity.ok(response);
    }

    /**
     * Check authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> authStatus(Authentication authentication) {
        log.debug("Checking authentication status");

        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal())) {

            log.debug("User is authenticated: {}", authentication.getName());

            if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                String picture = oauth2User.getAttribute("picture");

                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "user", Map.of(
                        "email", email != null ? email : "",
                        "name", name != null ? name : "",
                        "picture", picture != null ? picture : ""
                    )
                ));
            }
            
            return ResponseEntity.ok(Map.of("authenticated", true));
        }

        return ResponseEntity.ok(Map.of("authenticated", false));
    }
}

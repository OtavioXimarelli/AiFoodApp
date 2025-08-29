package com.otavio.aifoodapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otavio.aifoodapp.controller.base.BaseController;
import com.otavio.aifoodapp.dto.UserDTO;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Consolidated controller that handles all user-related operations:
 * - Authentication (login/logout)
 * - User information
 * - Profile management
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController extends BaseController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
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
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Logged out successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * Get authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> status = new HashMap<>();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            status.put("authenticated", true);

            switch (auth.getPrincipal()) {
                case OAuth2User oauth2User -> {
                    status.put("authType", "oauth2");
                    status.put("email", oauth2User.getAttribute("email"));
                    status.put("name", oauth2User.getAttribute("name"));
                }
                case User user -> {
                    status.put("authType", "session");
                    status.put("email", user.getEmail());
                }
                default -> {
                    status.put("authType", "other");
                    status.put("principalType", auth.getPrincipal().getClass().getName());
                }
            }
        } else {
            status.put("authenticated", false);
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Get detailed user information for the authenticated user
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = getCurrentUser();
            if (user != null) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("id", user.getId());
                profile.put("email", user.getEmail());
                profile.put("firstName", user.getFirstName());
                profile.put("lastName", user.getLastName());
                profile.put("profilePicture", user.getProfilePicture());
                profile.put("role", user.getRole());
                return ResponseEntity.ok(profile);
            }
        }

        return ResponseEntity.status(401).build();
    }
}

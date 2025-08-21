package com.otavio.aifoodapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otavio.aifoodapp.dto.UserDTO;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;
import com.otavio.aifoodapp.security.TokenService;
import com.otavio.aifoodapp.service.FoodItemService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador consolidado para autenticação
 * Combina funcionalidades de status, login, logout e gerenciamento de tokens
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final FoodItemService foodItemService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    // Cache para controlar a frequência de verificações por sessão
    private final Map<String, Long> lastStatusChecks = new ConcurrentHashMap<>();
    private static final long STATUS_CHECK_THROTTLE_MS = 2000; // 2 segundos
    
    public AuthController(FoodItemService foodItemService, TokenService tokenService, UserRepository userRepository) {
        this.foodItemService = foodItemService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }
    
    
    /**
     * Verificar informações do usuário atual
     * Endpoint alternativo para obter dados do usuário autenticado
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
     * Endpoint para logout
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
     * Endpoint para obter URL de login do Google
     */
    @GetMapping("/login/google")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", "/oauth2/authorization/google");
        response.put("message", "Redirect to this URL to login with Google");
        return ResponseEntity.ok(response);
    }

    /**
     * Verificar se o usuário está autenticado e retornar detalhes
     * Usado pelo frontend para verificar autenticação persistente
     */
    @GetMapping("/status")
    public ResponseEntity<?> authStatus(HttpServletRequest request) {
        log.info("=== AUTH STATUS CHECK ===");
        log.info("Request URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
        log.info("Remote IP: {}, User-Agent: {}", request.getRemoteAddr(), request.getHeader("User-Agent"));
        log.info("Host: {}, Origin: {}, Referer: {}", 
                request.getHeader("Host"), 
                request.getHeader("Origin"), 
                request.getHeader("Referer"));

        try {
            // Get session ID or IP if no session exists
            HttpSession session = request.getSession(false);
            String sessionId = session != null ? session.getId() : request.getRemoteAddr();
            
            // Check request frequency for this endpoint
            long now = System.currentTimeMillis();
            Long lastCheck = lastStatusChecks.get(sessionId);
            
            // If last check was too recent, return 429 Too Many Requests
            if (lastCheck != null && now - lastCheck < STATUS_CHECK_THROTTLE_MS) {
                log.warn("Too many requests to /api/auth/status from {}", sessionId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Too many requests. Please wait."));
            }
            
            // Update last check timestamp
            lastStatusChecks.put(sessionId, now);
            
            // Limit cache size (remove old entries with 1% probability)
            if (lastStatusChecks.size() > 1000 && Math.random() < 0.01) {
                cleanOldEntries();
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Debug session information
            log.debug("Auth status check with session ID: {}", sessionId);
            log.debug("Request URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
            log.debug("User-Agent: {}", request.getHeader("User-Agent"));
            log.debug("Origin: {}", request.getHeader("Origin"));
            log.debug("Referer: {}", request.getHeader("Referer"));
            
            // Check cookies for diagnostics
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                log.debug("No cookies present in status request");
            } else {
                boolean foundJsessionId = false;
                for (Cookie cookie : cookies) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        foundJsessionId = true;
                        log.debug("JSESSIONID cookie found: domain={}, path={}, maxAge={}, secure={}, httpOnly={}",
                                cookie.getDomain() != null ? cookie.getDomain() : "default",
                                cookie.getPath(),
                                cookie.getMaxAge(),
                                cookie.getSecure(),
                                cookie.isHttpOnly());
                    }
                }
                if (!foundJsessionId) {
                    log.warn("JSESSIONID cookie not found in status request");
                }
            }
            
            // Create session if it doesn't exist to ensure we have a valid session for all requests
            if (session == null) {
                session = request.getSession(true);
                log.debug("Created new session for auth status check: {}", session.getId());
            }
            
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                log.info("User is authenticated as: {}", auth.getName());
                
                try {
                    // Ensure security context is in session
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    
                    // Get current user info from service
                    User user = foodItemService.getUserForTesting();
                    if (user != null) {
                        log.debug("Successfully retrieved user details for: {}", user.getEmail());
                        return ResponseEntity.ok(Map.of(
                            "authenticated", true,
                            "user", Map.of(
                                "id", user.getId(),
                                "name", user.getFirstName() != null && user.getLastName() != null ? 
                                       user.getFirstName() + " " + user.getLastName() : user.getEmail(),
                                "email", user.getEmail(),
                                "picture", user.getProfilePicture() != null ? user.getProfilePicture() : ""
                            ),
                            "sessionId", session.getId(),
                            "sessionCreatedAt", new java.util.Date(session.getCreationTime()).toString()
                        ));
                    } else {
                        log.warn("User object is null for authenticated user: {}", auth.getName());
                        return ResponseEntity.ok(Map.of(
                            "authenticated", true,
                            "sessionId", session.getId(),
                            "error", "User details unavailable",
                            "message", "Session is authenticated but user details cannot be retrieved"
                        ));
                    }
                } catch (NullPointerException e) {
                    log.error("NullPointerException in auth status check", e);
                    // Return basic authentication info without user details
                    return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "sessionId", session.getId(),
                        "error", "User details unavailable",
                        "message", "Session is authenticated but user details cannot be retrieved"
                    ));
                } catch (Exception e) {
                    log.error("Error getting user details: {}", e.getMessage(), e);
                    return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "sessionId", session.getId(),
                        "error", "Error fetching user details",
                        "message", e.getMessage()
                    ));
                }
            } else {
                log.debug("User is not authenticated. Auth: {}", auth);
            }
            
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("Unexpected error in auth status endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Internal server error",
                    "message", e.getMessage(),
                    "path", request.getRequestURI()
                ));
        }
    }
    
    /**
     * Manually trigger token refresh if needed
     * This can be called by the frontend to ensure tokens are valid
     * @param request the HTTP request
     * @return Success status of the refresh operation
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth instanceof OAuth2AuthenticationToken oauth2Auth) {
            try {
                boolean refreshed = tokenService.refreshAccessTokenIfNeeded(oauth2Auth);
                return ResponseEntity.ok(Map.of(
                    "success", refreshed,
                    "message", refreshed ? "Token refreshed successfully" : "Token refresh not needed"
                ));
            } catch (Exception e) {
                log.error("Error refreshing token", e);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Error refreshing token: " + e.getMessage()
                ));
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "success", false,
            "message", "Not an OAuth2 authentication"
        ));
    }
    
    /**
     * Limpa entradas antigas do cache de verificações de status
     */
    private void cleanOldEntries() {
        long currentTime = System.currentTimeMillis();
        long threshold = currentTime - (STATUS_CHECK_THROTTLE_MS * 10); // 10x o tempo de throttle
        
        lastStatusChecks.entrySet().removeIf(entry -> entry.getValue() < threshold);
        log.debug("Cache de verificações de status limpo. Tamanho atual: {}", lastStatusChecks.size());
    }
}

package com.otavio.aifoodapp.controller;

import java.util.Map;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller para fornecer informações do usuário autenticado
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class UserInfoController {

    private final UserService userService;

    public UserInfoController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint para obter informações do usuário autenticado
     * Este endpoint é chamado pelo frontend após verificar que o usuário está autenticado
     */
    @GetMapping("/auth")
    public ResponseEntity<?> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            log.debug("Fornecendo informações para usuário autenticado: {}", auth.getName());
            
            try {
                User user = userService.findUserByEmail(auth.getName());
                
                if (user == null) {
                    log.warn("Usuário autenticado não encontrado na base de dados: {}", auth.getName());
                    return ResponseEntity.ok(Map.of(
                        "error", "user_not_found",
                        "authenticated", true
                    ));
                }
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("firstName", user.getFirstName());
                userInfo.put("lastName", user.getLastName());
                userInfo.put("name", user.getFirstName() + " " + user.getLastName());
                userInfo.put("profilePicture", user.getProfilePicture());
                userInfo.put("role", user.getRole().toString());
                userInfo.put("lastLoginAt", user.getLastLoginAt());
                
                return ResponseEntity.ok(userInfo);
            } catch (Exception e) {
                log.error("Erro ao buscar informações do usuário: {}", e.getMessage(), e);
                return ResponseEntity.ok(Map.of(
                    "error", "internal_error",
                    "authenticated", true,
                    "message", "Erro ao buscar informações do usuário"
                ));
            }
        }
        
        log.debug("Tentativa de acesso a /api/auth sem autenticação");
        return ResponseEntity.status(401).body(Map.of(
            "error", "unauthorized",
            "authenticated", false,
            "message", "Usuário não autenticado"
        ));
    }
}

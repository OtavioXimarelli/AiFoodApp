package com.otavio.aifoodapp.controller;

import com.otavio.aifoodapp.dto.UserDTO;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")

public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    public AuthenticationController(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;

    }


   @GetMapping
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.notFound().build();
        }

        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttributes().toString();
            String name = oAuth2User.getAttributes().toString();


            Optional<User> dbUser = userRepository.findByEmail(email);
            if (dbUser.isPresent()) {
                return ResponseEntity.ok(UserDTO.fromUser(dbUser.get()));
            } else {
                UserDTO userDTO = new UserDTO(null, email, name, "USER");
                return ResponseEntity.ok(userDTO);
            }
        }
        return ResponseEntity.status(401).build();
   }
}
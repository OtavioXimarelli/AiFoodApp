package com.otavio.aifoodapp.controller;

import com.otavio.aifoodapp.dto.AuthenticationDTO;
import com.otavio.aifoodapp.dto.LoginResponseDTO;
import com.otavio.aifoodapp.dto.RegisterDTO;
import com.otavio.aifoodapp.exception.UserAlreadyExistsException;
import com.otavio.aifoodapp.exception.UsernameOrPasswordInvalidExcpetion;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;
import com.otavio.aifoodapp.security.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")

public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(AuthenticationManager authenticationManager, UserRepository userRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO data) {

        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            var token = tokenService.generateToken((User) auth.getPrincipal());
            return ResponseEntity.ok(new LoginResponseDTO(token, data.login(), auth.getAuthorities().toString()));

        } catch (BadCredentialsException e) {
            throw new UsernameOrPasswordInvalidExcpetion("Invalid username or password", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO data) {
        if (userRepository.findByLogin(data.login()) != null) throw new UserAlreadyExistsException("User already exists");

        String encryptedPassword = this.passwordEncoder.encode(data.password());

        User newUser = new User(data.login(), encryptedPassword, data.role());
        userRepository.save(newUser);

        return ResponseEntity.ok("User created");
    }
}
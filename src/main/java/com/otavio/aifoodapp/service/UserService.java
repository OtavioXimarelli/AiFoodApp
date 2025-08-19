package com.otavio.aifoodapp.service;

import org.springframework.stereotype.Service;

import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para manipulação de usuários
 */
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Busca um usuário pelo e-mail
     * @param email E-mail do usuário
     * @return Usuário encontrado ou null se não existir
     */
    public User findUserByEmail(String email) {
        if (email == null) {
            log.warn("Tentativa de busca de usuário com e-mail nulo");
            return null;
        }
        
        try {
            return userRepository.findByEmail(email)
                .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por e-mail: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Busca um usuário pelo ID
     * @param id ID do usuário
     * @return Usuário encontrado ou null se não existir
     */
    public User findUserById(Long id) {
        if (id == null) {
            log.warn("Tentativa de busca de usuário com ID nulo");
            return null;
        }
        
        try {
            return userRepository.findById(id)
                .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por ID: {}", e.getMessage(), e);
            return null;
        }
    }
}

package com.otavio.aifoodapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import com.otavio.aifoodapp.model.User;

public interface UserRepository extends JpaRepository<User, Long>{


    Optional<UserDetails> findByLogin(String login);
    
}

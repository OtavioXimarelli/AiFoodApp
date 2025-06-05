package com.otavio.aifoodapp.repository;

import com.otavio.aifoodapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<UserDetails> findByLogin(String login);

}

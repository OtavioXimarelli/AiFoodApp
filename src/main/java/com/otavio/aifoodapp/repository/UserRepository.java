package com.otavio.aifoodapp.repository;

import com.otavio.aifoodapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;


public interface UserRepository extends JpaRepository<User, Long> {


    UserDetails findByLogin(String login);

}

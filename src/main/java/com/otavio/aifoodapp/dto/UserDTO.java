package com.otavio.aifoodapp.dto;

import com.otavio.aifoodapp.model.User;

public record UserDTO(
        Long id,
        String email,
        String name,
        String role
) {
    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole() != null ? user.getRole().toString() : "USER"
        );
    }
}
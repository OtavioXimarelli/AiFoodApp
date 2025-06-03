package com.otavio.aifoodapp.dto;

import com.otavio.aifoodapp.enums.UserRoles;

public record RegisterDTO(String login, String password, UserRoles role) {
}

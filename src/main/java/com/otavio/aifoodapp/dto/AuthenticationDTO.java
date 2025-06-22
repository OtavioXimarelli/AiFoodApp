package com.otavio.aifoodapp.dto;

import jakarta.validation.constraints.NotNull;

public record AuthenticationDTO(@NotNull(message = " Login and password should not be empty") String login, String password) {
}

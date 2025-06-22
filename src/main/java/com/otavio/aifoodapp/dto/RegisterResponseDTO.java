package com.otavio.aifoodapp.dto;

import jakarta.validation.constraints.NotEmpty;

public record RegisterResponseDTO(@NotEmpty(message = " Login and password should not be empty") String login, String password) {
}

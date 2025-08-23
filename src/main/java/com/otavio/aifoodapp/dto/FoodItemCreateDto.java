package com.otavio.aifoodapp.dto;

import java.time.LocalDate;

public record FoodItemCreateDto(
    String name,
    Integer quantity,
    LocalDate expiration
) {}

package com.otavio.aifoodapp.dto;

import java.time.LocalDate;

public record FoodDto(
        Long id,
        String name,
        Integer quantity,
        LocalDate expiration,
        Double calories,
        Double protein,
        Double fat,
        Double carbohydrates,
        Double fiber,
        Double sugar,
        Double sodium,
        String foodGroup,
        String tags
) {}
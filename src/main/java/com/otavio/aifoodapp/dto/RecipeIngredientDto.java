package com.otavio.aifoodapp.dto;

public record RecipeIngredientDto(
    Long id,
    String name,
    Double quantity,
    String unit
) {}

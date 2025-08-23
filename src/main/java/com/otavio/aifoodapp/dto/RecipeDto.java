package com.otavio.aifoodapp.dto;

import java.util.List;
import java.util.Set;

public record RecipeDto(
    Long id,
    String name,
    String description,
    List<String> nutritionalInfo,
    List<String> instructions,
    Set<RecipeIngredientDto> ingredientsList
) {}

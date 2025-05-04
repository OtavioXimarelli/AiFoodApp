package com.otavio.aifoodapp.dto;


import com.otavio.aifoodapp.model.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class RecipeDto {
    private Long id;
    private String name;
    private String description;
    private List<String> nutritionalInfo;
    private List<String> instructions;
   private Set<RecipeIngredientDto> ingredientsList;



}

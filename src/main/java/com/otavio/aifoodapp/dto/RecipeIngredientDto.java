package com.otavio.aifoodapp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientDto {
    private Long id;
    private String name;
    private Double quantity;
    private String unit;


}
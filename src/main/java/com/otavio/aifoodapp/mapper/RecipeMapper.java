package com.otavio.aifoodapp.mapper;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.otavio.aifoodapp.dto.RecipeDto;
import com.otavio.aifoodapp.dto.RecipeIngredientDto;
import com.otavio.aifoodapp.model.Recipe;
import com.otavio.aifoodapp.model.RecipeIngredient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    private static final Logger logger = LoggerFactory.getLogger(RecipeMapper.class);
    private final ObjectMapper objectMapper;

    public RecipeMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    private static class AiRecipeDTO {
        public String dishName;
        public String prepTime;
        public List<String> instructions;
        public List<String> nutritionalInfo;
    }

    public List<Recipe> parseRecipeFromJson(String recipeJson) {
        String sanitizedJson = recipeJson.trim();
        if (sanitizedJson.startsWith("```json")) {
            sanitizedJson = sanitizedJson.substring(7);
        } else if (sanitizedJson.startsWith("```")) {
            sanitizedJson = sanitizedJson.substring(3);
        }

        if (sanitizedJson.endsWith("```")) {
            sanitizedJson = sanitizedJson.substring(0, sanitizedJson.length() - 3);
        }

        sanitizedJson = sanitizedJson.trim();


        try {
            List<AiRecipeDTO> dtos = objectMapper.readValue(sanitizedJson, new TypeReference<List<AiRecipeDTO>>() {
            });

            return dtos.stream().map(this::mapDtoToEntity).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error parsing the  json: {} ", sanitizedJson, e);
            return Collections.emptyList();
        }
    }

    private Recipe mapDtoToEntity(AiRecipeDTO dto) {
        Recipe recipe = new Recipe();
        recipe.setName(dto.dishName);
        recipe.setDescription(dto.prepTime);
        recipe.setInstructions(dto.instructions);
        recipe.setNutritionalInfo(dto.nutritionalInfo);
        recipe.setQuantity(1);
        recipe.setExpiration("");
        return recipe;
    }

    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeDto dto = new RecipeDto();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setInstructions(recipe.getInstructions());
        dto.setNutritionalInfo(recipe.getNutritionalInfo());

        if (recipe.getIngredientsList() != null) {
            Set<RecipeIngredientDto> ingredientsDto = recipe.getIngredientsList().stream()
                    .map(this::toIngredientDto)
                    .collect(Collectors.toSet());
            dto.setIngredientsList(ingredientsDto);

        }
        return dto;
    }

    public List<RecipeDto> toDto(List<Recipe> recipes) {
        if (recipes == null) {
            return Collections.emptyList();
        }
        return recipes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private RecipeIngredientDto toIngredientDto(RecipeIngredient recipeIngredient) {
        RecipeIngredientDto      dto = new RecipeIngredientDto();
        dto.setId(recipeIngredient.getId());
        dto.setQuantity(recipeIngredient.getQuantity());
        dto.setUnit(recipeIngredient.getUnit());
        return dto;
    }
}

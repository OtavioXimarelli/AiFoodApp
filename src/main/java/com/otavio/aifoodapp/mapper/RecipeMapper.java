package com.otavio.aifoodapp.mapper;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.otavio.aifoodapp.model.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
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
}

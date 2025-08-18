package com.otavio.aifoodapp.service;

import java.util.Collections;
import java.util.List;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.otavio.aifoodapp.config.MaritacaChatClient;
import com.otavio.aifoodapp.enums.FoodGroup;
import com.otavio.aifoodapp.model.FoodItem;

import reactor.core.publisher.Mono;

@Service
public class FoodAiService {

    private final MaritacaChatClient maritacaChatClient;
    private final ObjectMapper objectMapper;

    @Value("${maritaca.system.prompt}")
    private String systemPrompt;

    public FoodAiService(MaritacaChatClient maritacaChatClient, ObjectMapper objectMapper) {
        this.maritacaChatClient = maritacaChatClient;
        this.objectMapper = objectMapper;
    }

    public Mono<FoodItem> determineNutritionalFacts(FoodItem foodItem) {
        String promptText = String.format("""
                Determine nutritional facts for the following food item:

                Name: %s
                Quantity: %d

                Provide a JSON response with the following structure (all values should be numeric with no units):
                {
                  "calories": numeric_value,
                  "protein": numeric_value_in_grams,
                  "fat": numeric_value_in_grams,
                  "carbohydrates": numeric_value_in_grams,
                  "fiber": numeric_value_in_grams,
                  "sugar": numeric_value_in_grams,
                  "sodium": numeric_value_in_milligrams,
                  "foodGroup": "one_of_[FRUITS, VEGETABLES, GRAINS, PROTEIN, DAIRY, FATS_OILS, BEVERAGES, SWEETS_SNACKS]",
                  "tags": ["tag1", "tag2"]
                }

                Note: Provide best estimates based on nutritional databases. The foodGroup must be one of the exact values listed.
                """,
                foodItem.getName(), foodItem.getQuantity());

        Prompt nutritionPrompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(promptText)
        ));

        return maritacaChatClient.call(nutritionPrompt)
                .map(response -> response.getResult().getOutput().getText())
                .map(jsonResponse -> parseNutritionResponse(jsonResponse, foodItem))
                .onErrorReturn(foodItem); // Return original food item on error
    }

    private FoodItem parseNutritionResponse(String jsonResponse, FoodItem foodItem) {
        try {
            // Clean the JSON string if needed (e.g., removing markdown formatting)
            String cleanedJson = jsonResponse.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7);
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
            cleanedJson = cleanedJson.trim();

            JsonNode jsonNode = objectMapper.readTree(cleanedJson);

            // Update nutritional facts
            foodItem.setCalories(jsonNode.has("calories") ? jsonNode.get("calories").asDouble() : null);
            foodItem.setProtein(jsonNode.has("protein") ? jsonNode.get("protein").asDouble() : null);
            foodItem.setFat(jsonNode.has("fat") ? jsonNode.get("fat").asDouble() : null);
            foodItem.setCarbohydrates(jsonNode.has("carbohydrates") ? jsonNode.get("carbohydrates").asDouble() : null);
            foodItem.setFiber(jsonNode.has("fiber") ? jsonNode.get("fiber").asDouble() : null);
            foodItem.setSugar(jsonNode.has("sugar") ? jsonNode.get("sugar").asDouble() : null);
            foodItem.setSodium(jsonNode.has("sodium") ? jsonNode.get("sodium").asDouble() : null);

            // Set food group
            if (jsonNode.has("foodGroup")) {
                String foodGroupStr = jsonNode.get("foodGroup").asText();
                try {
                    foodItem.setFoodGroup(FoodGroup.valueOf(foodGroupStr));
                } catch (IllegalArgumentException e) {
                    // Default to GRAINS if the food group is invalid
                    foodItem.setFoodGroup(FoodGroup.GRAINS);
                }
            }

            // Set tags
            if (jsonNode.has("tags") && jsonNode.get("tags").isArray()) {
                List<String> tags = objectMapper.convertValue(jsonNode.get("tags"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                foodItem.setTags(tags);
            } else {
                foodItem.setTags(Collections.emptyList());
            }

            return foodItem;
        } catch (JsonProcessingException e) {
            return foodItem; // Return original food item on error
        }
    }
}

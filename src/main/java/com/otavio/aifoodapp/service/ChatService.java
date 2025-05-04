package com.otavio.aifoodapp.service;


import com.otavio.aifoodapp.config.MaritacaChatClient;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.Recipe;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final MaritacaChatClient maritacaChatClient;


    @Value("${maritaca.system.prompt}")
    private String systemPrompt;

    public ChatService(MaritacaChatClient maritacaChatClient) {
        this.maritacaChatClient = maritacaChatClient;
    }

    public Mono<String> generateRecipe(List<FoodItem> foodItems) {
        String food = foodItems.stream()
                .map(item -> String.format("%s- Quantidade: %d, Validade: %s",
                        item.getName(), item.getQuantity(),
                        item.getExpiration()))
                .collect(Collectors.joining("\n"));

        //Prompt de teste
//        String prompt = "Me mostre qual a estrutura do nosso banco de dados e quais itens estao armazenados nele";

        String prompt = "Generate a recipe strictly following the criteria below.\n\n" +
                "**Available Ingredients:**\n" +
                food + "\n\n" +
                "**Recipe Criteria:**\n" +
                "1.  **Language:** Portuguese\n" +
                "2.  **Servings:** 1 person\n" +
                "3.  **Style:** Practical (simple steps), quick (fitness-focused), healthy, and tasty.\n" +
                "4.  **Mandatory Output:**\n" +
                "    * Dish Name (or what it contains)\n" +
                "    * Estimated Total Preparation Time\n" +
                "    * Detailed Preparation Method (step by step)\n" +
                "    * Nutritional Estimate (Calories, Proteins, Carbohydrates, Fats)\n\n" +
                "**Note:** The answer must be in Portuguese.";


        Prompt chatPrompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(prompt)
        ));

        return maritacaChatClient.call(chatPrompt)
                .map(response -> response.getResult().getOutput().getText());
    }

    public Mono<String> analyzeNutritionalProfile(Recipe recipe) {
        String food = String.format("%s- Quantidade: %d, Validade: %s",
                recipe.getName(), recipe.getQuantity(), recipe.getExpiration());

        String nutriPrompt = "Analyze the nutritional profile of the following food item:\n\n" +
                "**Food Item:**\n" +
                food + "\n\n" +
                "**Criteria:**\n" +
                "1.  **Language:** Portuguese\n" +
                "2.  **Output Required:**\n" +
                "    * Nutritional Profile (Calories, Proteins, Carbohydrates, Fats)\n" +
                "    * Suggestions for a balanced diet based on the provided food item.\n";

        Prompt analyzePrompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(nutriPrompt)));

        return maritacaChatClient.call(analyzePrompt)
                .map(response -> response.getResult().getOutput().getText());
    }

    public Mono<String> suggestDietaryAdjustments(List<FoodItem> foodItems, String dietaryPreference) {

        return null;
    }
}
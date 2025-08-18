package com.otavio.aifoodapp.service;


import java.util.List;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.otavio.aifoodapp.config.MaritacaChatClient;
import com.otavio.aifoodapp.mapper.RecipeMapper;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.Recipe;

import reactor.core.publisher.Mono;
// import java.util.stream.Collectors; // Unused import removed

@Service
public class ChatService {
    private final MaritacaChatClient maritacaChatClient;
    private final RecipeMapper recipeMapper;


    @Value("${maritaca.system.prompt}")
    private String systemPrompt;

    public ChatService(MaritacaChatClient maritacaChatClient, RecipeMapper recipeMapper) {
        this.maritacaChatClient = maritacaChatClient;
        this.recipeMapper = recipeMapper;
    }

    public Mono<List<Recipe>> generateRecipe(List<FoodItem> foodItems) {
        // Generated food list string but not using it in this implementation
        // Keeping the code for future reference
        /* 
        String food = foodItems.stream()
                .map(item -> String.format("%s- Quantidade: %d, Validade: %s",
                        item.getName(), item.getQuantity(),
                        item.getExpiration()))
                .collect(Collectors.joining("\n"));
        */

        //Prompt de teste
//        String prompt = "Me mostre qual a estrutura do nosso banco de dados e quais itens estao armazenados nele";

        String prompt = """
                Gere uma ou mais receitas... A resposta DEVE ser um array JSON válido. Cada objeto no array deve ter a seguinte estrutura: 
                {"dishName": "NOME_DO_PRATO", "prepTime": "TEMPO_DE_PREPARO", "instructions": ["Passo 1", "Passo 2"], "nutritionalInfo": ["Calorias: X kcal", "Proteínas: Y g"]}
                """;


        Prompt chatPrompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(prompt)
        ));

        return maritacaChatClient.call(chatPrompt)
                .map(response -> response.getResult().getOutput().getText())
                .map(recipeMapper::parseRecipeFromJson);
    }

    public Mono<String> analyzeNutritionalProfile(Recipe recipe) {
        String food = String.format("%s- Quantidade: %d, Validade: %s",
                recipe.getName(), recipe.getQuantity(), recipe.getExpiration());

        String nutriPrompt = """
                Analyze the nutritional profile of the following food item:

                **Food Item:**
                """ + food + """

                **Criteria:**
                1.  **Language:** Portuguese
                2.  **Output Required:**
                    * Nutritional Profile (Calories, Proteins, Carbohydrates, Fats)
                    * Suggestions for a balanced diet based on the provided food item.
                """;

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
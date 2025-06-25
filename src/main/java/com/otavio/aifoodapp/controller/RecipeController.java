package com.otavio.aifoodapp.controller;

import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.Recipe;
import com.otavio.aifoodapp.service.FoodItemService;
import com.otavio.aifoodapp.service.ChatService;
import com.otavio.aifoodapp.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    private final FoodItemService foodItemService;
    private final ChatService chatService;
    private final RecipeService recipeService;

    public RecipeController(FoodItemService foodItemService, ChatService chatService, RecipeService recipeService) {
        this.foodItemService = foodItemService;
        this.chatService = chatService;
        this.recipeService = recipeService;
    }

    @GetMapping("/gen")
    public Mono<ResponseEntity<List<Recipe>>> generateRecipe() {
        List<FoodItem> foodItems = foodItemService.listAll();
        return chatService.generateRecipe(foodItems)
                .map(recipes -> recipes.stream()
                        .map(recipeService::save)
                        .collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/analyze/{id}")
    public Mono<ResponseEntity<String>> analyzeRecipe(@PathVariable Long id) {
        logger.info("Received request to analyze recipe with id: {}", id);
        return Mono.justOrEmpty(recipeService.findById(id))
                .doOnNext(recipe -> logger.info("Found recipe: {}", recipe.getName()))
                .flatMap(recipe -> {
                    logger.info("Calling chatService.analyzeNutritionalProfile for recipe id: {}", id);
                    return chatService.analyzeNutritionalProfile(recipe)
                            .doOnNext(result -> logger.info("Received nutritional analysis for recipe id {}: {}", id, result));
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
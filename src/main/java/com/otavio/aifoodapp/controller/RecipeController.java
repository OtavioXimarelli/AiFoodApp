package com.otavio.aifoodapp.controller;

import com.otavio.aifoodapp.dto.RecipeDto;
import com.otavio.aifoodapp.mapper.RecipeMapper;
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
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    private final FoodItemService foodItemService;
    private final ChatService chatService;
    private final RecipeService recipeService;
    private final RecipeMapper recipeMapper;

    public RecipeController(FoodItemService foodItemService, ChatService chatService, RecipeService recipeService, RecipeMapper recipeMapper) {
        this.foodItemService = foodItemService;
        this.chatService = chatService;
        this.recipeService = recipeService;
        this.recipeMapper = recipeMapper;
    }

    @GetMapping("/gen")
    public Mono<ResponseEntity<List<RecipeDto>>> generateRecipe() {
        List<FoodItem> foodItems = foodItemService.listAll();
        return chatService.generateRecipe(foodItems)
                .flatMap(recipesToSave ->
                        Mono.fromCallable(() -> recipeService.saveAllAndInitialize(recipesToSave))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(recipeMapper::toDto)
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
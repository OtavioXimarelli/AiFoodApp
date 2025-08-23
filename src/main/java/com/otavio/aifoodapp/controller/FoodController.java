package com.otavio.aifoodapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otavio.aifoodapp.controller.base.BaseController;
import com.otavio.aifoodapp.dto.FoodDto;
import com.otavio.aifoodapp.dto.FoodItemCreateDto;
import com.otavio.aifoodapp.dto.RecipeDto;
import com.otavio.aifoodapp.mapper.FoodMapper;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.Recipe;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.service.ChatService;
import com.otavio.aifoodapp.service.FoodItemService;
import com.otavio.aifoodapp.service.RecipeService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Consolidated controller for managing food items and recipes.
 * Extends BaseController to leverage common functionality.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class FoodController extends BaseController {

    private final FoodItemService foodItemService;
    private final FoodMapper foodMapper;
    private final ChatService chatService;
    private final RecipeService recipeService;

    public FoodController(
            FoodItemService foodItemService,
            FoodMapper foodMapper,
            ChatService chatService,
            RecipeService recipeService) {
        this.foodItemService = foodItemService;
        this.foodMapper = foodMapper;
        this.chatService = chatService;
        this.recipeService = recipeService;
    }

    // ================= FOOD ITEM ENDPOINTS =================

    /**
     * List all food items for the current user
     */
    @GetMapping("/food-items")
    public ResponseEntity<?> listFoods() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            List<FoodItem> foodItems = foodItemService.listAllForUser(currentUser);
            List<FoodDto> foodDtos = foodItems.stream()
                    .map(foodMapper::map)
                    .toList();

            return success(foodDtos);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Get a specific food item by ID
     */
    @GetMapping("/food-items/{id}")
    public ResponseEntity<?> getFoodById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            FoodItem foodItem = foodItemService.getById(id);
            if (foodItem == null) {
                return notFound("Food item", id);
            }

            if (!isAuthorized(foodItem.getUser().getId())) {
                return unauthorized();
            }

            return success(foodMapper.map(foodItem));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Create a new food item with AI enhancement
     */
    @PostMapping("/food-items")
    public ResponseEntity<?> createFood(@Valid @RequestBody FoodItemCreateDto createDto) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            log.info("Creating food item: {}", createDto.name());

            FoodItem foodItem = foodItemService.createWithAiEnhancement(createDto, currentUser);
            return success(foodMapper.map(foodItem));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Update an existing food item
     */
    @PutMapping("/food-items/{id}")
    public ResponseEntity<?> updateFood(@PathVariable Long id, @Valid @RequestBody FoodItemCreateDto createDto) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            FoodItem existingItem = foodItemService.getById(id);
            if (existingItem == null) {
                return notFound("Food item", id);
            }

            if (!isAuthorized(existingItem.getUser().getId())) {
                return unauthorized();
            }

            FoodItem updatedItem = foodItemService.update(id, createDto, currentUser);
            return success(foodMapper.map(updatedItem));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Delete a food item
     */
    @DeleteMapping("/food-items/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            FoodItem existingItem = foodItemService.getById(id);
            if (existingItem == null) {
                return notFound("Food item", id);
            }

            if (!isAuthorized(existingItem.getUser().getId())) {
                return unauthorized();
            }

            foodItemService.delete(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Food item deleted successfully");
            return success(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ================= RECIPE ENDPOINTS =================

    /**
     * Generate recipes based on available food items
     */
    @GetMapping("/recipes/gen")
    public Mono<ResponseEntity<List<RecipeDto>>> generateRecipe() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        List<FoodItem> foodItems = foodItemService.listAllForUser(currentUser);

        return chatService.generateRecipe(foodItems)
                .flatMap(recipes ->
                        Mono.fromCallable(() -> recipeService.saveAndMapToDto(recipes))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error generating recipes", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    /**
     * Get a specific recipe by ID
     */
    @GetMapping("/recipes/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            // First check if recipe exists and user has access using entity
            Optional<Recipe> recipeEntity = recipeService.findById(id);
            if (recipeEntity.isEmpty()) {
                return notFound("Recipe", id);
            }
            
            if (!isAuthorized(recipeEntity.get().getUser().getId())) {
                return unauthorized();
            }
            
            // If authorized, get the DTO version
            Optional<RecipeDto> recipeDto = recipeService.getById(id);
            if (recipeDto.isPresent()) {
                return success(recipeDto.get());
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * List all recipes for the current user
     */
    @GetMapping("/recipes")
    public ResponseEntity<?> listRecipes() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            List<RecipeDto> recipes = recipeService.getAllRecipesForUser(currentUser);
            return success(recipes);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Delete a recipe
     */
    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return unauthorized();
            }

            // First check if recipe exists and user has access using entity
            Optional<Recipe> recipeEntity = recipeService.findById(id);
            if (recipeEntity.isEmpty()) {
                return notFound("Recipe", id);
            }
            
            if (!isAuthorized(recipeEntity.get().getUser().getId())) {
                return unauthorized();
            }

            recipeService.delete(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recipe deleted successfully");
            return success(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }
}

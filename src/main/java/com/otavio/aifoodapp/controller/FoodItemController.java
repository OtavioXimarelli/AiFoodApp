package com.otavio.aifoodapp.controller;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otavio.aifoodapp.dto.FoodDto;
import com.otavio.aifoodapp.dto.FoodItemCreateDto;
import com.otavio.aifoodapp.mapper.FoodMapper;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.service.FoodItemService;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = {"http://localhost:5173", "https://aifoodapp.site"}, allowCredentials = "true")
public class FoodItemController {
    private final FoodItemService foodItemService;
    private final FoodMapper foodMapper;


    public FoodItemController(FoodItemService foodItemService, FoodMapper foodMapper) {
        this.foodItemService = foodItemService;
        this.foodMapper = foodMapper;
    }
    
    /**
     * Create a single food item with simplified data and AI enhancement
     * User will only provide name, quantity, and expiration date
     * AI will automatically determine all nutritional facts and food group
     * @param createDto DTO with name, quantity, and expiration only
     * @return The complete food item with AI-determined nutritional facts
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody FoodItemCreateDto createDto) {
        try {
            System.out.println("Received create food request: " + createDto.getName());
            FoodItem foodItem = foodMapper.map(createDto);
            FoodItem savedItem = foodItemService.saveWithAiEnhancement(foodItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(foodMapper.map(savedItem));
        } catch (Exception e) {
            e.printStackTrace();
            // Return error details for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", e.getMessage(),
                "type", e.getClass().getName()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodDto> getById(@PathVariable Long id) {
        return foodItemService.listById(id)
                .map(foodMapper::map)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<FoodDto>> list() {
        try {
            List<FoodItem> foodItems = foodItemService.listAll();
            List<FoodDto> foodDtos = foodItems.stream()
                    .map(foodMapper::map)
                    .toList();
            return ResponseEntity.ok(foodDtos);
        } catch (Exception e) {
            // Log the exception details for debugging
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                // Also try to get user information through service
                try {
                    User user = foodItemService.getUserForTesting();
                    return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "principal_type", auth.getPrincipal().getClass().getName(),
                        "principal", auth.getPrincipal().toString(),
                        "authorities", auth.getAuthorities().toString(),
                        "name", auth.getName(),
                        "user_found", true,
                        "user_id", user.getId().toString(),
                        "user_email", user.getEmail() != null ? user.getEmail() : "null"
                    ));
                } catch (Exception userEx) {
                    return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "principal_type", auth.getPrincipal().getClass().getName(),
                        "principal", auth.getPrincipal().toString(),
                        "authorities", auth.getAuthorities().toString(), 
                        "name", auth.getName(),
                        "user_found", false,
                        "user_error", userEx.getMessage()
                    ));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "authenticated", false,
                    "auth_object", auth != null ? auth.toString() : "null"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", e.getMessage(),
                "type", e.getClass().getName()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody FoodItemCreateDto createDto) {
        // First check if the item exists and belongs to the user
        Optional<FoodItem> foodItemOpt = foodItemService.listById(id);
        if (foodItemOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("The item was not found, please try again and check the item ID");
        }
        
        // Map the createDto to a FoodItem and set the ID
        FoodItem foodItem = foodMapper.map(createDto);
        foodItem.setId(id);
        
        // Use AI to update nutritional information
        FoodItem updatedFood = foodItemService.saveWithAiEnhancement(foodItem);
        return ResponseEntity.ok(foodMapper.map(updatedFood));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (foodItemService.listById(id).isPresent()) {
            foodItemService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
    }
}

package com.otavio.aifoodapp.controller;


import java.util.List;
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

import com.otavio.aifoodapp.dto.FoodDto;
import com.otavio.aifoodapp.dto.FoodItemCreateDto;
import com.otavio.aifoodapp.mapper.FoodMapper;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.service.FoodItemService;

@RestController
@RequestMapping("/api/foods")
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
    public ResponseEntity<FoodDto> create(@Valid @RequestBody FoodItemCreateDto createDto) {
        FoodItem foodItem = foodMapper.map(createDto);
        FoodItem savedItem = foodItemService.saveWithAiEnhancement(foodItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(foodMapper.map(savedItem));
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
        List<FoodItem> foodItems = foodItemService.listAll();
        List<FoodDto> foodDtos = foodItems.stream()
                .map(foodMapper::map)
                .toList();
        return ResponseEntity.ok(foodDtos);
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

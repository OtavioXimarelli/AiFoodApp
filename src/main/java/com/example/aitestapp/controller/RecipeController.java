package com.example.aitestapp.controller;


import com.example.aitestapp.model.FoodItem;
import com.example.aitestapp.service.FoodItemService;
import com.example.aitestapp.service.TextGenService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class RecipeController {

    private final FoodItemService foodItemService;
    private final TextGenService textGenService;

    public RecipeController(FoodItemService foodItemService, TextGenService textGenService) {
        this.foodItemService = foodItemService;
        this.textGenService = textGenService;
    }

    @GetMapping("/gen")
    public Mono<ResponseEntity<String>> generateRecipe() {
        List<FoodItem> foodItem = foodItemService.listAll();
        return textGenService.generateRecipe(foodItem)
                .map(recipe -> ResponseEntity.ok(recipe))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

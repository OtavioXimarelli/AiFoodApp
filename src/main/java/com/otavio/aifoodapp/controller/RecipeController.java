package com.otavio.aifoodapp.controller;


import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.service.FoodItemService;
import com.otavio.aifoodapp.service.TextGenService;

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

package com.otavio.aifoodapp.controller;


import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.service.FoodItemService;
import com.otavio.aifoodapp.service.ChatService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final FoodItemService foodItemService;
    private final ChatService chatService;

    public RecipeController(FoodItemService foodItemService, ChatService chatService) {
        this.foodItemService = foodItemService;
        this.chatService = chatService;
    }

    @GetMapping("/gen")
    public Mono<ResponseEntity<Mono<String>>> generateRecipe() {
        List<FoodItem> foodItem = foodItemService.listAll();
       return  Mono.justOrEmpty(chatService.generateRecipe(foodItem))
               .map(ResponseEntity::ok)
               .defaultIfEmpty(ResponseEntity.notFound().build());

        }
}

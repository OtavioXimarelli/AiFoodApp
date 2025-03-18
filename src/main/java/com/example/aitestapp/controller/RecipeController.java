package com.example.aitestapp.controller;


import com.example.aitestapp.service.TextGenService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class RecipeController {

    private final TextGenService textGenService;

    public RecipeController(TextGenService textGenService) {
        this.textGenService = textGenService;
    }

    @GetMapping("/gen")
    public Mono<ResponseEntity<String>> generateRecipe() {
        return textGenService.generateRecipe()
                .map(recipe -> ResponseEntity.ok(recipe))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

package com.otavio.aifoodapp.service;

import com.otavio.aifoodapp.model.Recipe;
import com.otavio.aifoodapp.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }


    public Recipe save(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public List<Recipe> saveAll(List<Recipe> recipes) {return recipeRepository.saveAll(recipes);}

    public List<Recipe> listAll() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> findById(Long id) {
        Optional<Recipe> idExists = recipeRepository.findById(id);
        if (idExists.isPresent()) {
            return idExists;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe with id " + id + " not found");
        }
    }

    public Recipe update(Recipe recipe) {
        Optional<Recipe> recipeExists = recipeRepository.findById(recipe.getId());

        if (recipeExists.isPresent()) {
            return recipeRepository.save(recipe);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe with id " + recipe.getId() + " not found");
        }

    }
}

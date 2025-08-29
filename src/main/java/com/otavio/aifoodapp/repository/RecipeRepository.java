package com.otavio.aifoodapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.otavio.aifoodapp.model.Recipe;

public interface RecipeRepository extends JpaRepository <Recipe, Long> {
    List<Recipe> findByUserId(Long userId);
}

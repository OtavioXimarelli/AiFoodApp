package com.otavio.aifoodapp.repository;

import com.otavio.aifoodapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository <Recipe, Long> {
    // Custom query methods can be defined here if needed
}

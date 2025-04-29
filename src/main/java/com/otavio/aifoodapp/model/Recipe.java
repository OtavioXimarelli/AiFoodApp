package com.otavio.aifoodapp.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_recipes")

public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tb_recipe_instructions", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "step_order")
    @Column(name = "instruction")
    private List<String> instructions;


    private String nutritionalInfo;


    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RecipeIngredient> ingredientsList = new HashSet<>();


    public Recipe() {
    }

    public Recipe(String name, String description, List<String> instructions, String nutritionalInfo) {
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.nutritionalInfo = nutritionalInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public String getNutritionalInfo() {
        return nutritionalInfo;
    }

    public void setNutritionalInfo(String nutritionalInfo) {
        this.nutritionalInfo = nutritionalInfo;
    }

    public Set<RecipeIngredient> getIngredientsList() {
        return ingredientsList;
    }

    public void setIngredientsList(Set<RecipeIngredient> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }


}

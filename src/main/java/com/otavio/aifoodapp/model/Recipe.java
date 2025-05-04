package com.otavio.aifoodapp.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_recipes")

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    private int quantity;
    private String expiration;

    private List<String> nutritionalInfo;


    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RecipeIngredient> ingredientsList = new HashSet<>();



}

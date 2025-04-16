package com.otavio.aifoodapp.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "tb_food_item")
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private Integer quantity;
    private LocalDate expiration;
    private double calories;
    private double protein;
    private double fat;
    private double carbohydrates;
    private double fiber;
    private double sugar;
    private double sodium;





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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public double getCalories() {
        return calories;
    }
    public void setCalories(double calories) {
        this.calories = calories;
    }
    public double getProtein() {
        return protein;
    }
    public void setProtein(double protein) {
        this.protein = protein;
    }
    public double getFat() {
        return fat;
    }
    public void setFat(double fat) {
        this.fat = fat;
    }
    public double getCarbohydrates() {
        return carbohydrates;
    }
    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }
    public double getFiber() {
        return fiber;
    }
    public void setFiber(double fiber) {
        this.fiber = fiber;
    }
    public double getSugar() {
        return sugar;
    }
    public void setSugar(double sugar) {
        this.sugar = sugar;
    }
    public double getSodium() {
        return sodium;
    }
    public void setSodium(double sodium) {
        this.sodium = sodium;
    }
}

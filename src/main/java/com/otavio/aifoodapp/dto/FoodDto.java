package com.otavio.aifoodapp.dto;



import java.time.LocalDate;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodDto {
    private Long id;
    private String name;
    private Integer quantity;
    private LocalDate expiration;
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private Double fiber;
    private Double sugar;
    private Double sodium;
    private String foodGroup;
    private String tags;
}
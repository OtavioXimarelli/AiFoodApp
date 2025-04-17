package com.otavio.aifoodapp.dto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodDto {
    private Long id;
    private String name;
    private Integer quantity;
    private String expiration;
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
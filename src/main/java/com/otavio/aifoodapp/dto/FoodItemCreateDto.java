package com.otavio.aifoodapp.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodItemCreateDto {
    private String name;
    private Integer quantity;
    private LocalDate expiration;
}

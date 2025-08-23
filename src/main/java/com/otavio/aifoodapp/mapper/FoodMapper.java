package com.otavio.aifoodapp.mapper;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.otavio.aifoodapp.dto.FoodDto;
import com.otavio.aifoodapp.dto.FoodItemCreateDto;
import com.otavio.aifoodapp.enums.FoodGroup;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.User;

@Component
public class FoodMapper {
    public FoodItem map(FoodDto foodDto) {
        FoodItem foodItem = new FoodItem();
        foodItem.setId(foodDto.id());
        foodItem.setName(foodDto.name());
        foodItem.setQuantity(foodDto.quantity());
        foodItem.setExpiration(foodDto.expiration());
        foodItem.setCalories(foodDto.calories());
        foodItem.setProtein(foodDto.protein());
        foodItem.setFat(foodDto.fat());
        foodItem.setCarbohydrates(foodDto.carbohydrates());
        foodItem.setFiber(foodDto.fiber());
        foodItem.setSugar(foodDto.sugar());
        foodItem.setSodium(foodDto.sodium());
        foodItem.setFoodGroup(foodDto.foodGroup() != null ? FoodGroup.valueOf(foodDto.foodGroup()) : null);
        foodItem.setTags(foodDto.tags() != null ? List.of(foodDto.tags().split(",")) : new ArrayList<>());
        return foodItem;
    }

    public FoodItem map(FoodItemCreateDto createDto) {
        FoodItem foodItem = new FoodItem();
        foodItem.setName(createDto.name());
        foodItem.setQuantity(createDto.quantity());
        foodItem.setExpiration(createDto.expiration());
        // Other fields will be populated by the AI service
        return foodItem;
    }

    public FoodItem map(FoodItemCreateDto createDto, User user) {
        FoodItem foodItem = map(createDto);
        foodItem.setUser(user);
        return foodItem;
    }

    public FoodDto map(FoodItem foodItem) {
        return new FoodDto(
            foodItem.getId(),
            foodItem.getName(),
            foodItem.getQuantity(),
            foodItem.getExpiration(),
            foodItem.getCalories(),
            foodItem.getProtein(),
            foodItem.getFat(),
            foodItem.getCarbohydrates(),
            foodItem.getFiber(),
            foodItem.getSugar(),
            foodItem.getSodium(),
            foodItem.getFoodGroup() != null ? foodItem.getFoodGroup().name() : null,
            foodItem.getTags() != null ? String.join(",", foodItem.getTags()) : ""
        );
    }
}

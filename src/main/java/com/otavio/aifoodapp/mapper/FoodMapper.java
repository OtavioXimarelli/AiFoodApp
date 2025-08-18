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
        foodItem.setId(foodDto.getId());
        foodItem.setName(foodDto.getName());
        foodItem.setQuantity(foodDto.getQuantity());
        foodItem.setExpiration(foodDto.getExpiration());
        foodItem.setCalories(foodDto.getCalories());
        foodItem.setProtein(foodDto.getProtein());
        foodItem.setFat(foodDto.getFat());
        foodItem.setCarbohydrates(foodDto.getCarbohydrates());
        foodItem.setFiber(foodDto.getFiber());
        foodItem.setSugar(foodDto.getSugar());
        foodItem.setSodium(foodDto.getSodium());
        foodItem.setFoodGroup(foodDto.getFoodGroup() != null ? FoodGroup.valueOf(foodDto.getFoodGroup()) : null);
        foodItem.setTags(foodDto.getTags() != null ? List.of(foodDto.getTags().split(",")) : new ArrayList<>());
        return foodItem;
    }

    public FoodItem map(FoodItemCreateDto createDto) {
        FoodItem foodItem = new FoodItem();
        foodItem.setName(createDto.getName());
        foodItem.setQuantity(createDto.getQuantity());
        foodItem.setExpiration(createDto.getExpiration());
        // Other fields will be populated by the AI service
        return foodItem;
    }

    public FoodItem map(FoodItemCreateDto createDto, User user) {
        FoodItem foodItem = map(createDto);
        foodItem.setUser(user);
        return foodItem;
    }

    public FoodDto map(FoodItem foodItem) {
        FoodDto foodDto = new FoodDto();
        foodDto.setId(foodItem.getId());
        foodDto.setName(foodItem.getName());
        foodDto.setQuantity(foodItem.getQuantity());
        foodDto.setExpiration(foodItem.getExpiration());
        foodDto.setCalories(foodItem.getCalories());
        foodDto.setProtein(foodItem.getProtein());
        foodDto.setFat(foodItem.getFat());
        foodDto.setCarbohydrates(foodItem.getCarbohydrates());
        foodDto.setFiber(foodItem.getFiber());
        foodDto.setSugar(foodItem.getSugar());
        foodDto.setSodium(foodItem.getSodium());
        foodDto.setFoodGroup(foodItem.getFoodGroup() != null ? foodItem.getFoodGroup().name() : null);
        foodDto.setTags(foodItem.getTags() != null ? String.join(",", foodItem.getTags()) : "");
        return foodDto;
    }
}

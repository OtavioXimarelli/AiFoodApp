package com.otavio.aifoodapp.mapper;
import java.util.List;

import com.otavio.aifoodapp.dto.FoodDto;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.enums.FoodGroup;

import org.springframework.stereotype.Component;

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
        foodItem.setFoodGroup(FoodGroup.valueOf(foodDto.getFoodGroup()));
        foodItem.setTags(List.of(foodDto.getTags().split(",")));
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
        foodDto.setFoodGroup(foodItem.getFoodGroup().name());
        foodDto.setTags(String.join(",", foodItem.getTags()));
        return foodDto;
    }
}

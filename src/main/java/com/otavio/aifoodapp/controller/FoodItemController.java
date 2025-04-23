package com.otavio.aifoodapp.controller;


import com.otavio.aifoodapp.dto.FoodDto;
import com.otavio.aifoodapp.mapper.FoodMapper;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.service.FoodItemService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/foods")
public class FoodItemController {
    private final FoodItemService foodItemService;
    private final FoodMapper foodMapper;


    public FoodItemController(FoodItemService foodItemService, FoodMapper foodMapper) {
        this.foodItemService = foodItemService;
        this.foodMapper = foodMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<FoodDto> create(@RequestBody FoodDto foodDto) {
        FoodItem foodItem = foodMapper.map(foodDto);
        FoodItem savedFood = foodItemService.save(foodItem);

        return ResponseEntity.ok(foodMapper.map(savedFood));
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<Optional<FoodDto>> listById(@PathVariable Long id) {
        FoodItem foodItem = foodItemService.listById(id).orElse(null);
        assert foodItem != null;
        return ResponseEntity.ok(Optional.ofNullable(foodMapper.map(foodItem)));
    }

    @GetMapping("/list")
    public ResponseEntity<List<FoodDto>> list() {
        List<FoodItem> foodItems = foodItemService.listAll();
        List<FoodDto> foodDtos = foodItems.stream()
                .map(foodMapper::map)
                .toList();
        return ResponseEntity.ok(foodDtos);

    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody FoodDto foodDto) {

        FoodItem foodItem = foodMapper.map(foodDto);
        Optional<FoodItem> foodItemOpt = foodItemService.listById(foodItem.getId());
        if (foodItemOpt.isPresent()) {
            FoodItem updatedFood = foodItemService.modify(foodItem);
            return ResponseEntity.ok(foodMapper.map(updatedFood));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "The item was not found, please try again and check the item ID"
        );


    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable Long id) {
        Optional<FoodItem> foodItemDel = foodItemService.listById(id);
        if (foodItemDel.isPresent()) {
            foodItemService.delete(id);
            return ResponseEntity.ok(foodMapper.map(foodItemDel.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "The item was not found, please try again and check yhe item ID"
        );
    }
}

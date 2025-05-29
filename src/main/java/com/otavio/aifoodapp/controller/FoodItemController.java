package com.otavio.aifoodapp.controller;


import com.otavio.aifoodapp.dto.FoodDto;
import com.otavio.aifoodapp.mapper.FoodMapper;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.service.FoodItemService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<FoodDto>> create(@Valid @RequestBody List<FoodDto> foodDto) {
        List<FoodItem> foodItems = foodDto.stream()
                .map(foodMapper::map)
                .collect(Collectors.toList());

        List<FoodItem> savedItems = foodItemService.saveAll(foodItems);
        List<FoodDto> savedDto = savedItems.stream()
                .map(foodMapper::map)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);

    }

    @GetMapping("/list/{id}")
    public ResponseEntity<FoodDto> listById(@PathVariable Long id) {
        return foodItemService.listById(id)
                .map(foodMapper::map)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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
        if (foodItemService.listById(id).isPresent()) {
            foodItemService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
    }
}

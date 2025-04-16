package com.otavio.aifoodapp.controller;


import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.service.FoodItemService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/food")
public class FoodItemController {
    private final FoodItemService foodItemService;


    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @PostMapping("/create")
    public ResponseEntity<FoodItem> create(@RequestBody FoodItem foodItem) {

        FoodItem saved = foodItemService.save(foodItem);


        return ResponseEntity.status(200).body(saved);
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<Optional<FoodItem>> listById(@PathVariable Long id) {
        Optional<FoodItem> listId = foodItemService.listById(id);
            return ResponseEntity.status(200).body(listId);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FoodItem>> list() {

        List<FoodItem> allItems = foodItemService.listAll();
        return ResponseEntity.status(HttpStatus.OK).body(allItems);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody FoodItem foodItem) {
        FoodItem foodItemUpdated = foodItemService.modify(foodItem);
        if (foodItemUpdated != null) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    foodItemUpdated + "The item was successfully updated"
            );
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "The item was not found, please try again, make sure the ID is correct"
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFood (@PathVariable Long id) {
        Optional<FoodItem> foodItemDel = foodItemService.listById(id);
        if (foodItemDel.isPresent()) {
            foodItemService.delete(id);
                return ResponseEntity.status(HttpStatus.OK).body("The item was successfully deleted");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            "The item was not found, please try again and check yhe item ID"
            );
    }
}

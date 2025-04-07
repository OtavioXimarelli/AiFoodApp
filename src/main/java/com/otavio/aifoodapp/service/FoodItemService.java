package com.otavio.aifoodapp.service;

import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.repository.FoodItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    public FoodItemService(FoodItemRepository foodItemRepository, ThreadPoolTaskExecutor threadPoolTaskExecutor) {this.foodItemRepository = foodItemRepository;
    }


    public FoodItem save(FoodItem foodItem) { return foodItemRepository.save(foodItem);}

    public List<FoodItem> listAll (){return foodItemRepository.findAll();}

    public Optional<FoodItem> listById(Long id){
        Optional<FoodItem> idExists = foodItemRepository.findById(id);
        if (idExists.isPresent()) {
            return foodItemRepository.findById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + id + " not found");
        }
    }

    public FoodItem modify(FoodItem foodItem) {
        Optional<FoodItem> itemExist = foodItemRepository.findById(foodItem.getId());
        if (itemExist.isPresent()){
            return foodItemRepository.save(foodItem);
        }
        return null;
    }

    public void delete (Long id) {foodItemRepository.deleteById(id);}
}

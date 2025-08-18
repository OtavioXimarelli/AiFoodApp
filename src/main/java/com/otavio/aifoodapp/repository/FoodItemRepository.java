package com.otavio.aifoodapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.User;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByUser(User user);
    List<FoodItem> findByUserId(Long userId);
}

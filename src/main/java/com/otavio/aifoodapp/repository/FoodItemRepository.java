package com.otavio.aifoodapp.repository;

import com.otavio.aifoodapp.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    Long id;
}

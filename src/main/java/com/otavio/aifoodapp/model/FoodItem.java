package com.otavio.aifoodapp.model;


import java.time.LocalDate;
import java.util.List;

import com.otavio.aifoodapp.enums.FoodGroup;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "tb_food_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer quantity;
    private LocalDate expiration;
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private Double fiber;
    private Double sugar;
    private Double sodium;


    @Enumerated(EnumType.STRING)
    @Column(name = "food_group")
    // Enum para representar o grupo alimentar
    private FoodGroup foodGroup;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tb_food_tems_tags", joinColumns = @JoinColumn(name = "food_item_id"))  
    @Column(name = "tag")
    private List<String> tags;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

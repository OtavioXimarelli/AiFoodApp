package com.otavio.aifoodapp.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

import com.otavio.aifoodapp.enums.FoodGroup;
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
}

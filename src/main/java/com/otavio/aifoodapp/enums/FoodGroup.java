package com.otavio.aifoodapp.enums;

public enum FoodGroup {
    FRUITS("Frutas"),
    VEGETABLES("Vegetais"),
    GRAINS("Grãos"),
    PROTEINS("Proteínas"),
    DAIRY("Laticínios"),
    FATS_OILS("Gorduras e Óleos"),
    BEVERAGES("Bebidas"),
    SWEETS_SNACKS("Doces e Lanches"); 

    private final String groupName;

    FoodGroup(String groupName) { 
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    // Método para obter o enum a partir do nome de exibição (se necessário)
    public static FoodGroup fromGroupName(String groupName) {
        for (FoodGroup group : FoodGroup.values()) {
            if (group.groupName.equalsIgnoreCase(groupName)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Nenhum grupo alimentar encontrado com o nome: " + groupName);
    }
}
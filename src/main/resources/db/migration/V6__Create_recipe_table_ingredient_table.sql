CREATE TABLE tb_recipe_ingredients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT,
    unit TEXT,
    quantity DOUBLE PRECISION,

    FOREIGN KEY (recipe_id) REFERENCES tb_recipes(id),
    FOREIGN KEY (food_item_id) REFERENCES tb_food_item(id)

);
CREATE TABLE tb_recipe_ingredients (
                                       id BIGSERIAL PRIMARY KEY,
                                       recipe_id BIGINT NOT NULL,
                                       food_item_id BIGINT NOT NULL,
                                       unit TEXT,
                                       quantity DOUBLE PRECISION,
                                       CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES tb_recipes(id),
                                       CONSTRAINT fk_recipe_ingredients_food_item FOREIGN KEY (food_item_id) REFERENCES tb_food_item(id)
);
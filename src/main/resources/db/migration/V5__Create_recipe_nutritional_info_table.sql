CREATE TABLE tb_recipes_nutritional_info (
                                             id BIGSERIAL PRIMARY KEY,
                                             recipe_id BIGINT NOT NULL,
                                             nutritional_info TEXT NOT NULL,
                                             FOREIGN KEY (recipe_id) REFERENCES tb_recipes(id)
);
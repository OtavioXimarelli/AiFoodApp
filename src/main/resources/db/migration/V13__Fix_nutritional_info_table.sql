DROP TABLE IF EXISTS tb_recipes_instructions;

CREATE TABLE tb_recipes_nutritional_info (
    recipe_id BIGINT NOT NULL,
    nutritional_info TEXT NOT NULL,
    foreign key (recipe_id) references tb_recipes(id) ON DELETE CASCADE

);
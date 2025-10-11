DROP TABLE IF EXISTS tb_recipes_instructions;

CREATE TABLE tb_recipe_instructions
(
    recipe_id    BIGINT NOT NULL,
    step_order   INT    NOT NULL,
    instruction TEXT   NOT NULL,
    PRIMARY KEY (recipe_id, step_order),
    FOREIGN KEY (recipe_id) REFERENCES tb_recipes (id) ON DELETE CASCADE
);
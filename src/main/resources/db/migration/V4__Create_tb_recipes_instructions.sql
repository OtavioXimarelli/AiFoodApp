CREATE TABLE tb_recipes_instructions (
    recipe_id BIGINT NOT NULL ,
    step_order INT NOT NULL ,
    instructions VARCHAR(255),
    PRIMARY KEY (recipe_id, step_order) ,
    FOREIGN KEY (recipe_id) REFERENCES tb_recipes(id)

);
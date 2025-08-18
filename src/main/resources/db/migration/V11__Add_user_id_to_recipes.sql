ALTER TABLE tb_recipes 
ADD COLUMN user_id BIGINT;

ALTER TABLE tb_recipes
ADD CONSTRAINT fk_recipes_user
FOREIGN KEY (user_id)
REFERENCES tb_users(id);

CREATE INDEX idx_recipes_user_id ON tb_recipes(user_id);

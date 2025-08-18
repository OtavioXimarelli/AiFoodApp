ALTER TABLE tb_food_item 
ADD COLUMN user_id BIGINT;

ALTER TABLE tb_food_item
ADD CONSTRAINT fk_food_item_user
FOREIGN KEY (user_id)
REFERENCES tb_users(id);

CREATE INDEX idx_food_item_user_id ON tb_food_item(user_id);

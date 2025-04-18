ALTER TABLE tb_food_item
        ADD COLUMN calories DOUBLE PRECISION,
        ADD COLUMN protein DOUBLE PRECISION,
        ADD COLUMN fat DOUBLE PRECISION,
        ADD COLUMN carbohydrates DOUBLE PRECISION,
        ADD COLUMN fiber DOUBLE PRECISION,
        ADD COLUMN sugar DOUBLE PRECISION,
        ADD COLUMN sodium DOUBLE PRECISION,
        ADD COLUMN food_group VARCHAR(50);

ALTER TABLE tb_food_item
        DROP COLUMN type;


CREATE TABLE tb_food_items_tag (
                                   food_item_id BIGINT NOT NULL,
                                   tag VARCHAR(50) NOT NULL,
                                   FOREIGN KEY (food_item_id) REFERENCES tb_food_item(id) ON DELETE CASCADE
)
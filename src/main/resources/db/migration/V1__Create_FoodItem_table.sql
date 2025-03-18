CREATE TABLE tb_food_item (
    id IDENTITY PRIMARY KEY ,
    name VARCHAR(250) NOT NULL,
    type VARCHAR(250) NOT NULL ,
    quantity INTEGER NOT NULL,
    expiration DATE NOT NULL
);
CREATE TABLE tb_recipes (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            quantity INT,
                            expiration DATE
);
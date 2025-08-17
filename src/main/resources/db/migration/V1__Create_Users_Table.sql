CREATE TABLE tb_users (
                          id BIGSERIAL PRIMARY KEY,
                          login VARCHAR(50) UNIQUE, -- Nullable for Google-only users
                          password VARCHAR(255), -- Nullable for Google-only users
                          first_name VARCHAR(50),
                          last_name VARCHAR(50),
                          email VARCHAR(255) UNIQUE NOT NULL, -- Case-insensitive uniqueness enforced by index
                          role VARCHAR(20) NOT NULL DEFAULT 'USER',
                          google_id VARCHAR(64) UNIQUE, -- Google OAuth ID
                          profile_picture TEXT, -- URL to profile picture
                          provider VARCHAR(50) DEFAULT 'LOCAL', -- Authentication provider
                          is_active BOOLEAN DEFAULT TRUE, -- Active status
                          created_at TIMESTAMPTZ DEFAULT NOW(), -- Creation timestamp
                          last_login_at TIMESTAMPTZ -- Last login timestamp
);

-- Case-insensitive unique index for email
CREATE UNIQUE INDEX ux_tb_users_email ON tb_users (LOWER(email));

-- Unique index for Google ID
CREATE UNIQUE INDEX ux_tb_users_google_id ON tb_users (google_id);
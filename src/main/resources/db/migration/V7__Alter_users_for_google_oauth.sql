-- 1) Make password nullable (Google users won’t have one)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'tb_users' AND column_name = 'password' AND is_nullable = 'NO'
    ) THEN
ALTER TABLE tb_users ALTER COLUMN password DROP NOT NULL;
END IF;
END$$;

-- 2) Make login nullable (optional). You can keep it unique; Postgres allows multiple NULLs in a UNIQUE column.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'tb_users' AND column_name = 'login' AND is_nullable = 'NO'
    ) THEN
ALTER TABLE tb_users ALTER COLUMN login DROP NOT NULL;
END IF;
END$$;

-- 3) Ensure email exists; make it unique (case-insensitive). Only set NOT NULL when you’re sure all rows have an email.
ALTER TABLE tb_users
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- Backfill email from login when login looks like an email (optional)
UPDATE tb_users
SET email = login
WHERE email IS NULL
  AND login ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$';

-- Unique index on lower(email) for case-insensitive uniqueness
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'tb_users' AND indexname = 'ux_tb_users_email'
    ) THEN
CREATE UNIQUE INDEX ux_tb_users_email ON tb_users (LOWER(email));
END IF;
END$$;

-- If ready, enforce NOT NULL on email (optional)
-- ALTER TABLE tb_users ALTER COLUMN email SET NOT NULL;

-- 4) Add Google-related columns
ALTER TABLE tb_users
    ADD COLUMN IF NOT EXISTS google_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS profile_picture TEXT,
    ADD COLUMN IF NOT EXISTS provider VARCHAR(50) DEFAULT 'GOOGLE',
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- 5) Unique index on google_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'tb_users' AND indexname = 'ux_tb_users_google_id'
    ) THEN
CREATE UNIQUE INDEX ux_tb_users_google_id ON tb_users (google_id);
END IF;
END$$;
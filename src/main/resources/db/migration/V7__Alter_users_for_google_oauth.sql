-- 1) Add columns needed for Google OAuth
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS google_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS profile_picture TEXT,
    ADD COLUMN IF NOT EXISTS provider VARCHAR(50) DEFAULT 'GOOGLE',
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- 2) If password was NOT NULL, make it nullable to allow passwordless Google users
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'password'
          AND is_nullable = 'NO'
    ) THEN
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
END IF;
END$$;

-- 3) Backfill email from login when login looks like an email (optional, only run if you used login as email)
UPDATE users
SET email = login
WHERE email IS NULL
  AND login ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$';

-- 4) Ensure email is unique and not null going forward (if your business requires email)
-- First drop any existing duplicate emails before adding the constraint, or skip NOT NULL if migrating slowly.
-- Make NOT NULL only if you're sure all rows have an email.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'users' AND indexname = 'ux_users_email'
    ) THEN
CREATE UNIQUE INDEX ux_users_email ON users (LOWER(email));
END IF;
END$$;

-- If you’re ready to enforce NOT NULL on email (optional):
-- ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- 5) Optional index on google_id for quick lookups
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'users' AND indexname = 'ux_users_google_id'
    ) THEN
CREATE UNIQUE INDEX ux_users_google_id ON users (google_id);
END IF;
END$$;
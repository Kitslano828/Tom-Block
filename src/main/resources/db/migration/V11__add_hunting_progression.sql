ALTER TABLE tomblock.player_profiles
    ADD COLUMN IF NOT EXISTS hunting_xp BIGINT NOT NULL DEFAULT 0 CHECK (hunting_xp >= 0);

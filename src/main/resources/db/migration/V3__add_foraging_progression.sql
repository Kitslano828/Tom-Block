ALTER TABLE tomblock.player_profiles
    ADD COLUMN IF NOT EXISTS foraging_xp BIGINT NOT NULL DEFAULT 0 CHECK (foraging_xp >= 0);

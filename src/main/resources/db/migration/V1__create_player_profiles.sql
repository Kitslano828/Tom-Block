CREATE TABLE IF NOT EXISTS tomblock.player_profiles (
    player_id UUID PRIMARY KEY,
    mining_xp BIGINT NOT NULL DEFAULT 0 CHECK (mining_xp >= 0),
    combat_xp BIGINT NOT NULL DEFAULT 0 CHECK (combat_xp >= 0),
    skill_reward_version SMALLINT NOT NULL DEFAULT 2,
    prosperity DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tomblock.player_base_stats (
    player_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    stat_key VARCHAR(64) NOT NULL,
    stat_value DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (player_id, stat_key)
);

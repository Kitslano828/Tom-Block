CREATE TABLE IF NOT EXISTS tomblock.critter_journal (
    player_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    critter_id VARCHAR(128) NOT NULL,
    knowledge VARCHAR(32) NOT NULL,
    observations INTEGER NOT NULL DEFAULT 0 CHECK (observations >= 0),
    successful_hunts INTEGER NOT NULL DEFAULT 0 CHECK (successful_hunts >= 0),
    first_seen_at TIMESTAMPTZ NOT NULL,
    last_seen_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (player_id, critter_id)
);

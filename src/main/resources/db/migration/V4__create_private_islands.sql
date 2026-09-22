CREATE TABLE IF NOT EXISTS tomblock.private_islands (
    island_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL UNIQUE REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    world_name VARCHAR(80) NOT NULL UNIQUE,
    preset_key VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tomblock.private_island_members (
    island_id UUID NOT NULL REFERENCES tomblock.private_islands(island_id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    role VARCHAR(24) NOT NULL CHECK (role IN ('OWNER', 'MEMBER', 'VISITOR')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (island_id, player_id)
);

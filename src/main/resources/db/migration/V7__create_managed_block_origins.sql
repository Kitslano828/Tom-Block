CREATE TABLE IF NOT EXISTS tomblock.managed_block_origins (
    world_name VARCHAR(80) NOT NULL,
    block_x INTEGER NOT NULL,
    block_y INTEGER NOT NULL,
    block_z INTEGER NOT NULL,
    placed_by UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    placed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (world_name, block_x, block_y, block_z)
);

CREATE INDEX IF NOT EXISTS managed_block_origins_player_idx
    ON tomblock.managed_block_origins (placed_by);

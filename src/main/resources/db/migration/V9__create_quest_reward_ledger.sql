CREATE TABLE tomblock.quest_reward_deliveries (
    player_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    delivery_id VARCHAR(320) NOT NULL,
    claimed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (player_id, delivery_id)
);

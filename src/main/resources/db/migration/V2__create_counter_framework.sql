CREATE TABLE tomblock.counter_definitions (
    counter_key VARCHAR(128) PRIMARY KEY,
    display_name VARCHAR(128) NOT NULL,
    category VARCHAR(64) NOT NULL,
    description VARCHAR(512) NOT NULL DEFAULT '',
    unit VARCHAR(32) NOT NULL DEFAULT 'COUNT',
    default_value BIGINT NOT NULL DEFAULT 0,
    minimum_value BIGINT NOT NULL DEFAULT 0,
    maximum_value BIGINT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (default_value >= minimum_value),
    CHECK (maximum_value IS NULL OR maximum_value >= default_value)
);

CREATE TABLE tomblock.player_counters (
    player_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    counter_key VARCHAR(128) NOT NULL REFERENCES tomblock.counter_definitions(counter_key),
    amount BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (player_id, counter_key)
);

CREATE INDEX player_counters_counter_leaderboard_idx
    ON tomblock.player_counters (counter_key, amount DESC);

CREATE TABLE tomblock.player_quests (
    player_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    quest_id VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_stage_id VARCHAR(128),
    revision BIGINT NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    PRIMARY KEY (player_id, quest_id)
);

CREATE TABLE tomblock.player_quest_objectives (
    player_id UUID NOT NULL,
    quest_id VARCHAR(128) NOT NULL,
    stage_id VARCHAR(128) NOT NULL,
    objective_id VARCHAR(128) NOT NULL,
    amount BIGINT NOT NULL DEFAULT 0 CHECK (amount >= 0),
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (player_id, quest_id, stage_id, objective_id),
    FOREIGN KEY (player_id, quest_id)
        REFERENCES tomblock.player_quests(player_id, quest_id) ON DELETE CASCADE
);

CREATE INDEX player_quests_status_idx
    ON tomblock.player_quests (player_id, status);

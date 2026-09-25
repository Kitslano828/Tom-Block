CREATE TABLE tomblock.encounter_sessions (
    instance_id UUID PRIMARY KEY,
    definition_id VARCHAR(128) NOT NULL,
    owner_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    state VARCHAR(32) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    disconnect_deadline TIMESTAMPTZ,
    revision BIGINT NOT NULL DEFAULT 0,
    failure_reason VARCHAR(256)
);
CREATE TABLE tomblock.encounter_participants (
    instance_id UUID NOT NULL REFERENCES tomblock.encounter_sessions(instance_id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES tomblock.player_profiles(player_id) ON DELETE CASCADE,
    PRIMARY KEY(instance_id, player_id)
);
CREATE INDEX encounter_owner_state_idx ON tomblock.encounter_sessions(owner_id, state);

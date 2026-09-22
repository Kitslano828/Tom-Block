UPDATE tomblock.private_islands
SET world_name = world_name || '_v2',
    preset_key = 'STARTER_V2',
    updated_at = CURRENT_TIMESTAMP
WHERE preset_key = 'STARTER_V1';

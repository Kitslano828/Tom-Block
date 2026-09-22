package org.tomdang.island.runtime;

import java.util.UUID;

public record IslandContext(IslandRuntime island, UUID playerId, IslandRole role) { }

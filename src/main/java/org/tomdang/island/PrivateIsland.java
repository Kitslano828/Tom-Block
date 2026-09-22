package org.tomdang.island;

import java.util.UUID;

public record PrivateIsland(UUID islandId, UUID ownerId, String worldName, String presetKey) {
	public PrivateIsland {
		if (islandId == null || ownerId == null) throw new IllegalArgumentException("Island and owner ids are required");
		if (worldName == null || !worldName.matches("[a-z0-9_]{1,80}")) throw new IllegalArgumentException("Invalid world name");
		if (presetKey == null || presetKey.isBlank()) throw new IllegalArgumentException("Preset key is required");
	}

	public static PrivateIsland starter(UUID ownerId) {
		UUID id = UUID.randomUUID();
		return new PrivateIsland(id, ownerId, "island_" + id.toString().replace("-", ""), "PRIVATE_STARTER");
	}
}

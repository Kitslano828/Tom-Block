package org.tomdang.island.runtime;

import org.tomdang.island.preset.IslandPreset;

import java.util.UUID;

public record IslandRuntime(String worldName, IslandPreset preset, UUID islandId, UUID ownerId) {
	public IslandRuntime {
		if (worldName == null || worldName.isBlank() || preset == null) throw new IllegalArgumentException("World and preset are required");
	}
}

package org.tomdang.island.preset;

import java.util.Set;

public record IslandPreset(String id, String displayName, IslandClassification primaryType,
		Set<IslandTag> tags, IslandLifecycleMode lifecycleMode, int unloadDelaySeconds,
		String worldName, String generator, int travelRadius, double spawnX, double spawnY, double spawnZ,
		IslandInteractionPolicy interactions) {
	public IslandPreset {
		if (id == null || !id.matches("[A-Z][A-Z0-9_]*")) throw new IllegalArgumentException("Invalid island preset id");
		if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name is required");
		if (primaryType == null || lifecycleMode == null || interactions == null) throw new IllegalArgumentException("Incomplete island preset");
		if (tags == null) throw new IllegalArgumentException("Tags are required");
		tags = Set.copyOf(tags);
		if (unloadDelaySeconds < 0 || travelRadius < 1) throw new IllegalArgumentException("Invalid island timing or radius");
		if (generator == null || generator.isBlank()) throw new IllegalArgumentException("Generator is required");
	}
	public boolean privateWorld() { return lifecycleMode == IslandLifecycleMode.PERSISTENT_PRIVATE; }
}

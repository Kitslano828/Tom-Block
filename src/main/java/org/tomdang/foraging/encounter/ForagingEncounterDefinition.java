package org.tomdang.foraging.encounter;

import java.util.List;

public record ForagingEncounterDefinition(String id, String displayName, String world, double toughness,
		int cooldownSeconds, long xp, String collectionId, List<EncounterNode> nodes) {
	public ForagingEncounterDefinition {
		nodes = List.copyOf(nodes);
		if (id == null || id.isBlank() || displayName == null || displayName.isBlank() || world == null || world.isBlank()
				|| toughness <= 0 || cooldownSeconds < 0 || xp < 0 || collectionId == null || collectionId.isBlank() || nodes.isEmpty())
			throw new IllegalArgumentException("Invalid foraging encounter definition");
	}
}

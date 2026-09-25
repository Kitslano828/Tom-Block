package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;

public record RegionEntered(UUID playerId, String regionId) implements GameplayEvent {
	public RegionEntered {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (regionId == null || regionId.isBlank()) throw new IllegalArgumentException("regionId cannot be blank");
	}
}

package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;

public record MobDefeated(UUID playerId, String mobId) implements GameplayEvent {
	public MobDefeated {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (mobId == null || mobId.isBlank()) throw new IllegalArgumentException("mobId cannot be blank");
	}
}

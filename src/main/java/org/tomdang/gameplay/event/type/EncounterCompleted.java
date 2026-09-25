package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;

public record EncounterCompleted(UUID playerId, String encounterId) implements GameplayEvent {
	public EncounterCompleted {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (encounterId == null || encounterId.isBlank()) throw new IllegalArgumentException("encounterId cannot be blank");
	}
}

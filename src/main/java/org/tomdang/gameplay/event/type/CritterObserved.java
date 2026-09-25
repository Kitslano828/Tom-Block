package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;

public record CritterObserved(UUID playerId, String critterId) implements GameplayEvent {
	public CritterObserved {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (critterId == null || critterId.isBlank()) throw new IllegalArgumentException("critterId cannot be blank");
	}
}

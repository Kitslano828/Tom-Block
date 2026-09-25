package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;

public record ActorInteracted(UUID playerId, String actorId, UUID actorInstanceId) implements GameplayEvent {
	public ActorInteracted {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId cannot be blank");
	}
}

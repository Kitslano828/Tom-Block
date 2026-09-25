package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;

public record ItemCollected(UUID playerId, String itemId, long amount) implements GameplayEvent {
	public ItemCollected {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId cannot be blank");
		if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
	}
}

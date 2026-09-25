package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;

public record DialogueCompleted(UUID playerId, String dialogueId, String sourceId) implements GameplayEvent {
	public DialogueCompleted {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (dialogueId == null || dialogueId.isBlank()) throw new IllegalArgumentException("dialogueId cannot be blank");
		if (sourceId == null || sourceId.isBlank()) throw new IllegalArgumentException("sourceId cannot be blank");
	}
}

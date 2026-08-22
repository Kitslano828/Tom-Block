package org.tomdang.dialogueframework.context;

import java.util.UUID;

public record DialogueContext(String sourceID, UUID sourceInstanceID) {

	public DialogueContext {
		if (sourceID == null) throw new IllegalArgumentException("Source ID cannot be null");
		if (sourceID.isBlank()) throw new IllegalArgumentException("Source ID cannot be blank");
	}

	public static DialogueContext actor(String actorDefinitionID, UUID actorInstanceUUID) {
		if (actorDefinitionID == null) throw new IllegalArgumentException("ActorDefinitionID cannot be null");
		if (actorDefinitionID.isBlank()) throw new IllegalArgumentException("ActorDefinitionID cannot be blank");
		if (actorInstanceUUID == null) throw new IllegalArgumentException("ActorInstanceUUID cannot be null");

		return new DialogueContext(actorDefinitionID, actorInstanceUUID);
	}

	public static DialogueContext system(String sourceID) {
		return new DialogueContext(sourceID, null);
	}

}

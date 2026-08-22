package org.tomdang.actorframework.instance;

import lombok.Getter;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.definition.ActorDefinition;

import java.util.UUID;

public class ActorInstance {

	@Getter
	private final UUID instanceID;
	@Getter
	private final ActorDefinition actorDefinition;
	@Getter
	private final String spawnPointID;
	@Getter
	private final ActorAudienceKey audienceKey;

	public ActorInstance(UUID instanceID, ActorDefinition actorDefinition, String spawnPointID,  ActorAudienceKey audienceKey) {
		if (instanceID == null) throw new IllegalArgumentException("instance ID cannot be null");
		if (actorDefinition == null) throw new IllegalArgumentException("Actor definition cannot be null");
		if (audienceKey == null) throw new IllegalArgumentException("audience key cannot be null");
		if (spawnPointID != null && spawnPointID.isBlank()) throw new IllegalArgumentException("Spawn point ID cannot be blank");
		this.instanceID = instanceID;
		this.actorDefinition = actorDefinition;
		this.spawnPointID = spawnPointID;
		this.audienceKey = audienceKey;
	}

}

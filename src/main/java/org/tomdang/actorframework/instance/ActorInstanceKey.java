package org.tomdang.actorframework.instance;

import org.tomdang.actorframework.audience.ActorAudienceKey;

public record ActorInstanceKey(String actorID, ActorAudienceKey audienceKey, String spawnPointID) {

	public ActorInstanceKey {
		if (actorID == null) throw new IllegalArgumentException("Actor ID cannot be null!");
		if (actorID.isBlank()) throw new IllegalArgumentException("Actor ID cannot be empty");
		if (audienceKey == null) throw new IllegalArgumentException("audience Key cannot be null!");
		if (spawnPointID != null && spawnPointID.isBlank()) throw new IllegalArgumentException("SpawnPoint ID cannot be blank and non null");
	}

}

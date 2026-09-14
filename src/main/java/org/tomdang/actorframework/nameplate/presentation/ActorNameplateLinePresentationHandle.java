package org.tomdang.actorframework.nameplate.presentation;

import java.util.UUID;

public record ActorNameplateLinePresentationHandle(UUID presentationUUID, int entityID) {

	public ActorNameplateLinePresentationHandle {
		if (presentationUUID == null) throw new IllegalArgumentException("presentationUUID cannot be null");
		if (entityID < 1) throw new IllegalArgumentException("entityID must be positive");
	}

}

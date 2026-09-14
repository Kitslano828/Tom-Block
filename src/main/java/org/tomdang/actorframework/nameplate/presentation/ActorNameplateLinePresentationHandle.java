package org.tomdang.actorframework.nameplate.presentation;

import java.util.UUID;

public record ActorNameplateLinePresentationHandle(UUID presentationUUID, int entityID, double verticalOffset) {

	public ActorNameplateLinePresentationHandle {
		if (presentationUUID == null) throw new IllegalArgumentException("presentationUUID cannot be null");
		if (entityID < 1) throw new IllegalArgumentException("entityID must be positive");
		if (!Double.isFinite(verticalOffset)) throw new IllegalArgumentException("verticalOffset must be finite");
		if (verticalOffset < 0) throw new IllegalArgumentException("verticalOffset cannot be negative");
	}

}

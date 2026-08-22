package org.tomdang.actorframework.presentation;

import java.util.UUID;

public record ActorPresentationHandle(UUID presentationID, UUID actorInstanceID) {

	public ActorPresentationHandle {
		if (presentationID == null) throw new IllegalArgumentException("presentation ID cannot be null");
		if (actorInstanceID == null) throw new IllegalArgumentException("actor instance ID cannot be null");
	}

}

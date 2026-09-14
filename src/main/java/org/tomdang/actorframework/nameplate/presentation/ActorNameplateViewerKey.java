package org.tomdang.actorframework.nameplate.presentation;

import java.util.UUID;

public record ActorNameplateViewerKey(UUID instanceUUID, UUID viewerUUID) {

	public ActorNameplateViewerKey {
		if (instanceUUID == null) throw new IllegalArgumentException("instanceUUID cannot be null");
		if (viewerUUID == null) throw new IllegalArgumentException("viewerUUID cannot be null");
	}

}

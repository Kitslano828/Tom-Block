package org.tomdang.actorframework.presentation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveActorPresentationRegistry {

	private final Map<UUID, ActorPresentationHandle> presentationHandleMap = new HashMap<>();

	public void registerPresentation(ActorPresentationHandle presentationHandle) {
		if (presentationHandle == null) throw new IllegalArgumentException("Actor Presentation Handle cannot be null");
		if (presentationHandleMap.containsKey(presentationHandle.actorInstanceID())) throw new IllegalStateException("Actor presentation already exists!");
		UUID presentationHandleUUID = presentationHandle.actorInstanceID();
		presentationHandleMap.put(presentationHandleUUID, presentationHandle);
	}

	public ActorPresentationHandle getPresentation(UUID actorInstanceID) {
		if (actorInstanceID == null) throw new IllegalArgumentException("Actor Instance ID cannot be null");
		return presentationHandleMap.get(actorInstanceID);
	}

	public boolean hasPresentation(UUID actorInstanceID) {
		if (actorInstanceID == null) throw new IllegalArgumentException("Actor Instance ID cannot be null");
		return presentationHandleMap.containsKey(actorInstanceID);
	}

	public ActorPresentationHandle removePresentation(UUID actorInstanceID) {
		if (actorInstanceID == null) throw new IllegalArgumentException("Actor Instance ID cannot be null");
		if (!presentationHandleMap.containsKey(actorInstanceID)) return null;
		return presentationHandleMap.remove(actorInstanceID);
	}

	public ActorPresentationHandle getByPresentationID(UUID presentationID) {
		if (presentationID == null) throw new IllegalArgumentException("Presentation ID cannot be null");
		for (ActorPresentationHandle handle : presentationHandleMap.values()) {
			if (handle.presentationID().equals(presentationID)) return handle;
		}
		return null;
	}

}

package org.tomdang.actorframework.nameplate.presentation;

import java.util.*;

public class ActorNameplatePresentationRegistry {

	private final Map<ActorNameplateViewerKey, ActorNameplateViewerHandle> viewerHandles = new HashMap<>();

	public void register(ActorNameplateViewerHandle viewerHandle) {
		if (viewerHandle == null) throw new IllegalArgumentException("viewer handle cannot be null");

		ActorNameplateViewerKey key = viewerHandle.actorNameplateViewerKey();
		if (viewerHandles.containsKey(key)) throw new IllegalStateException("key already exist");
		viewerHandles.put(key, viewerHandle);
	}

	public boolean doesPresentationHandleExist(ActorNameplateViewerKey key) {
		if (key == null) throw new IllegalArgumentException("Key cannot be null");
		return viewerHandles.containsKey(key);
	}

	public ActorNameplateViewerHandle lookup(ActorNameplateViewerKey key) {
		if (key == null) throw new IllegalArgumentException("Key cannot be null");
		return viewerHandles.get(key);
	}

	public ActorNameplateViewerHandle replaceHandle(ActorNameplateViewerHandle handle) {
		if (handle == null) throw new IllegalArgumentException("handle cannot be null");

		ActorNameplateViewerKey key = handle.actorNameplateViewerKey();
		if (!viewerHandles.containsKey(key)) throw new IllegalStateException("There are no handles to be replaced");

		return viewerHandles.put(key, handle);
	}

	public ActorNameplateViewerHandle removeHandle(ActorNameplateViewerKey key) {
		if (key == null) throw new IllegalArgumentException("Key cannot be null");
		return viewerHandles.remove(key);
	}

	public List<ActorNameplateViewerHandle> findActorHandles(UUID instanceUUID) {
		if (instanceUUID == null) throw new IllegalArgumentException("instanceUUID cannot be null");

		List<ActorNameplateViewerHandle> actorHandles = new ArrayList<>();

		for (ActorNameplateViewerKey key : viewerHandles.keySet()) {
			if (key.instanceUUID().equals(instanceUUID)) actorHandles.add(lookup(key));
		}

		return List.copyOf(actorHandles);
	}

	public List<ActorNameplateViewerHandle> findViewerHandles(UUID viewerUUID) {
		if (viewerUUID == null) throw new IllegalArgumentException("viewerUUID cannot be null");

		List<ActorNameplateViewerHandle> viewerHandleList = new ArrayList<>();

		for (ActorNameplateViewerKey key : viewerHandles.keySet()) {
			if (key.viewerUUID().equals(viewerUUID)) viewerHandleList.add(lookup(key));
		}

		return List.copyOf(viewerHandleList);
	}

}

package org.tomdang.actorframework.nameplate.presentation;

import java.util.*;

public record ActorNameplateViewerHandle(ActorNameplateViewerKey actorNameplateViewerKey, List<ActorNameplateLinePresentationHandle> linePresentationHandles, boolean inMovingState) {

	public ActorNameplateViewerHandle {
		// Null & Empty Validation
		if (actorNameplateViewerKey == null) throw new IllegalArgumentException("actorNamePlateViewerKey cannot be null");
		if (linePresentationHandles == null || linePresentationHandles.isEmpty()) throw new IllegalArgumentException("linePresentationHandles cannot be null or empty");
		if (linePresentationHandles.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("linePresentationHandles cannot contain null elements");
		}

		// Requirement 2: Duplicate Identity Detection
		Set<UUID> seenPresentationUUIDs = new HashSet<>();
		Set<Integer> seenEntityIDs = new HashSet<>();

		for (ActorNameplateLinePresentationHandle handle : linePresentationHandles) {
			if (!seenPresentationUUIDs.add(handle.presentationUUID())) {
				throw new IllegalArgumentException("Duplicate presentation UUID found: " + handle.presentationUUID());
			}

			if (!seenEntityIDs.add(handle.entityID())) {
				throw new IllegalArgumentException("Duplicate entity ID found: " + handle.entityID());
			}
		}

		// Requirement 1: Defensive Copying
		// Assigning to linePresentationHandles in a compact constructor rebinds the record's field
		linePresentationHandles = List.copyOf(linePresentationHandles);
	}

}

package org.tomdang.actorframework.presentation;

import java.util.HashMap;
import java.util.Map;

public class ActorPresentationTypeRegistry {

	private final Map<String, ActorPresentation> actorPresentationMap = new HashMap<>();

	public void registerPresentation(String presentationType, ActorPresentation presentation) {
		if (presentationType == null) throw new IllegalArgumentException("presentation type cannot be null");
		if (presentationType.isBlank()) throw new IllegalArgumentException("presentation type cannot be blank");
		if (presentation == null) throw new IllegalArgumentException("Actor presentation cannot be null!");
		if (actorPresentationMap.containsKey(presentationType)) throw new IllegalStateException("Presentation type already exists");

		actorPresentationMap.put(presentationType, presentation);
	}

	public ActorPresentation lookupPresentation(String presentationType) {
		if (presentationType == null) throw new IllegalArgumentException("presentation type cannot be null");
		if (presentationType.isBlank()) throw new IllegalArgumentException("presentation type cannot be blank");
		return actorPresentationMap.get(presentationType);
	}

	public boolean isRegistered(String presentationType) {
		if (presentationType == null) throw new IllegalArgumentException("presentation type cannot be null");
		if (presentationType.isBlank()) throw new IllegalArgumentException("presentation type cannot be blank");
		return actorPresentationMap.containsKey(presentationType);
	}

}

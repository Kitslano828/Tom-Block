package org.tomdang.actorframework.interaction;

import java.util.HashMap;
import java.util.Map;

public class ActorInteractionRegistry 	{

	private final Map<String, ActorInteraction> actorInteractionMap = new HashMap<>();

	public void registerInteraction(String interactionID, ActorInteraction interaction) {
		if (interactionID == null) throw new IllegalArgumentException("Interaction ID cannot be null");
		if (interactionID.isBlank()) throw new IllegalArgumentException("Interaction ID cannot be blank");
		if (interaction == null) throw new IllegalArgumentException("Interaction cannot be null");
		if (actorInteractionMap.containsKey(interactionID)) throw new IllegalStateException("Interaction ID already exists");
		actorInteractionMap.put(interactionID, interaction);
	}

	public ActorInteraction lookupInteraction(String interactionID) {
		if (interactionID == null) throw new IllegalArgumentException("Interaction ID cannot be null");
		if (interactionID.isBlank()) throw new IllegalArgumentException("Interaction ID cannot be blank");
		return actorInteractionMap.get(interactionID);
	}

	public boolean isInteractionRegistered(String interactionID) {
		if (interactionID == null) throw new IllegalArgumentException("Interaction ID cannot be null");
		if (interactionID.isBlank()) throw new IllegalArgumentException("Interaction ID cannot be blank");
		return actorInteractionMap.containsKey(interactionID);
	}

}

package org.tomdang.actorframework.registry;

import org.tomdang.actorframework.definition.ActorDefinition;

import java.util.HashMap;
import java.util.Map;

public class ActorRegistry {

	private final Map<String, ActorDefinition> actorDefinitions = new HashMap<>();


	public void registerActor(ActorDefinition definition) {
		if (definition == null) throw new IllegalArgumentException("definition cannot be null!");
		String actorID = definition.getActorID();
		if (actorDefinitions.containsKey(actorID)) throw new IllegalStateException("Actor already exists");
		actorDefinitions.put(actorID, definition);
	}

	public ActorDefinition getActorDefinition(String actorID) {
		if (actorID == null) throw new IllegalArgumentException("ID cannot be null");
		if (actorID.isBlank()) throw new IllegalArgumentException("ID cannot be blank");
		return actorDefinitions.get(actorID);
	}

	public boolean isActorRegistered(String actorID) {
		if (actorID == null) throw new IllegalArgumentException("Actor ID cannot be null");
		if (actorID.isBlank()) throw new IllegalArgumentException("Actor ID cannot be blank");
		return actorDefinitions.containsKey(actorID);
	}

}

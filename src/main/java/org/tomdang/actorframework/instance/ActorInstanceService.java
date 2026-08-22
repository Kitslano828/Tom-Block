package org.tomdang.actorframework.instance;

import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.registry.ActorRegistry;

import java.util.UUID;

public class ActorInstanceService {

	private final ActorRegistry actorRegistry;
	private final ActorInstanceRegistry actorInstanceRegistry;

	public ActorInstanceService(ActorRegistry actorRegistry, ActorInstanceRegistry actorInstanceRegistry) {
		if (actorRegistry == null) throw new IllegalArgumentException("Actor Registry cannot be null");
		if (actorInstanceRegistry == null) throw new IllegalArgumentException("Actor instance registry cannot be null");

		this.actorRegistry = actorRegistry;
		this.actorInstanceRegistry = actorInstanceRegistry;
	}

	public ActorInstance restoreInstance(UUID storedInstanceUUID, String actorID, ActorAudienceKey key, String spawnPointID) {
		if (storedInstanceUUID == null) throw new IllegalArgumentException("Stored Instance UUID cannot be null");

		ActorDefinition definition = actorRegistry.getActorDefinition(actorID);
		if (definition == null) throw new IllegalStateException(actorID + " is null!");

		if (key == null) throw new IllegalArgumentException("The key's scope cannot be null");
		if (key.scope() != definition.getAudienceScope()) throw new IllegalStateException("The key's scope does not match the definition's");

		ActorInstance instance = new ActorInstance(storedInstanceUUID, definition, spawnPointID, key);
		actorInstanceRegistry.registerInstance(instance);

		return instance;
	}

	public ActorInstance createInstance(String actorID, ActorAudienceKey audienceKey, String spawnPointID) {
		ActorDefinition definition = actorRegistry.getActorDefinition(actorID);
		if (definition == null) throw new IllegalArgumentException("Unknown ID!" + actorID);
		if (audienceKey == null) throw new IllegalArgumentException("audience key cannot be null");
		if (audienceKey.scope() != definition.getAudienceScope()) throw new IllegalArgumentException("Scopes do not match!");

		ActorInstance instance = actorInstanceRegistry.getInstanceForPlacement(actorID, audienceKey, spawnPointID);
		if (instance != null) throw new IllegalStateException("An instance already exists for " + actorID + " and audience.");

		UUID actorInstanceUUID = UUID.randomUUID();
		ActorInstance actorInstance = new ActorInstance(actorInstanceUUID, definition, spawnPointID, audienceKey);
		actorInstanceRegistry.registerInstance(actorInstance);
		return actorInstance;
	}

	public ActorInstance getOrCreateInstance(String actorID, ActorAudienceKey audienceKey, String spawnPointID) {
		ActorInstance existingInstance = actorInstanceRegistry.getInstanceForPlacement(actorID, audienceKey, spawnPointID);
		if (existingInstance != null) return existingInstance;
		return createInstance(actorID, audienceKey, spawnPointID);
	}

	public ActorInstance removeInstance(UUID instanceUUID) {
		return actorInstanceRegistry.removeInstance(instanceUUID);
	}

}

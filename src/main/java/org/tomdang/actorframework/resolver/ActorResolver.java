package org.tomdang.actorframework.resolver;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.registry.ActorRegistry;

import java.util.UUID;

public class ActorResolver {

	private final NamespacedKey actorInstanceIDKey;
	private final NamespacedKey actorDefinitionIDKey;
	private final NamespacedKey audienceScopeKey;
	private final NamespacedKey audienceIDKey;
	private final NamespacedKey actorSpawnPointIDKey;
	private final ActorRegistry actorRegistry;
	private final ActorInstanceRegistry actorInstanceRegistry;

	public ActorResolver(NamespacedKey actorInstanceIDKey, NamespacedKey actorDefinitionIDKey,
	                     NamespacedKey audienceScopeKey, NamespacedKey audienceIDKey, NamespacedKey actorSpawnPointIDKey,
						 ActorRegistry actorRegistry, ActorInstanceRegistry actorInstanceRegistry
	) {
		if (actorInstanceIDKey == null) throw new IllegalArgumentException("Actor Instance ID Key cannot be null");
		if (actorDefinitionIDKey == null) throw new IllegalArgumentException("Actor Definition ID Key cannot be null");
		if (actorRegistry == null) throw new IllegalArgumentException("Actor Registry cannot be null");
		if (actorInstanceRegistry == null) throw new IllegalArgumentException("Actor Instance Registry cannot be null");
		if (audienceScopeKey == null) throw new IllegalArgumentException("audience scope key cannot be null");
		if (audienceIDKey == null) throw new IllegalArgumentException("audience id key cannot be null");
		if (actorSpawnPointIDKey == null) throw new IllegalArgumentException("actor spawn point id key cannot be null");

		this.actorInstanceIDKey = actorInstanceIDKey;
		this.actorDefinitionIDKey = actorDefinitionIDKey;
		this.audienceScopeKey = audienceScopeKey;
		this.audienceIDKey = audienceIDKey;
		this.actorSpawnPointIDKey = actorSpawnPointIDKey;
		this.actorRegistry = actorRegistry;
		this.actorInstanceRegistry = actorInstanceRegistry;
	}

	public String resolveSpawnPointID(Entity entity) {
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");

		ActorDefinition definition = resolveDefinition(entity);
		if (definition == null) return null;
		PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
		String spawnPointID = entityPDC.get(actorSpawnPointIDKey, PersistentDataType.STRING);

		if (spawnPointID != null && spawnPointID.isBlank()) throw new IllegalStateException(spawnPointID + " is not null but blank");

		return spawnPointID;
	}

	public ActorDefinition resolveDefinition(Entity entity) {
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");

		PersistentDataContainer entityPDC = entity.getPersistentDataContainer();

		if (!entityPDC.has(actorDefinitionIDKey, PersistentDataType.STRING)) return null;
		String actorID = entityPDC.get(actorDefinitionIDKey, PersistentDataType.STRING);

		if (!actorRegistry.isActorRegistered(actorID)) throw new IllegalStateException("The world contains an actor entity whose content definition no longer exists");

		return actorRegistry.getActorDefinition(actorID);
	}

	public ActorAudienceKey resolveAudienceKey(Entity entity) {
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");

		ActorDefinition definition = resolveDefinition(entity);
		if (definition == null) return null;

		PersistentDataContainer entityPDC = entity.getPersistentDataContainer();

		String storedScope = entityPDC.get(audienceScopeKey, PersistentDataType.STRING);

		if (storedScope == null) throw new IllegalStateException("Scope Key Does not exist");
		ActorAudienceScope parsedScope;
		try {
			parsedScope = ActorAudienceScope.valueOf(storedScope);

		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("entity contains invalid text.");
		}

		if (parsedScope != definition.getAudienceScope()) throw new IllegalStateException(parsedScope + " does not match " + definition.getAudienceScope());

		String storedAudienceID = entityPDC.get(audienceIDKey, PersistentDataType.STRING);

		if (parsedScope == ActorAudienceScope.GLOBAL) {
			if (storedAudienceID != null) throw new IllegalStateException(storedAudienceID + " should not exist for a global scope!");
			return ActorAudienceKey.global();
		}

		if (storedAudienceID == null) throw new IllegalStateException("Stored Audience ID doesn't exist");
		UUID parsedAudienceID;
		try {
			parsedAudienceID = UUID.fromString(storedAudienceID);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("Stored audience ID is not a valid UUID", exception);
		}

		return new ActorAudienceKey(parsedScope, parsedAudienceID);

	}

	public UUID resolveStoredInstanceID(Entity entity) {
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");

		ActorDefinition definition = resolveDefinition(entity);
		if (definition == null) return null;

		PersistentDataContainer entityPDC= entity.getPersistentDataContainer();
		if (!entityPDC.has(actorInstanceIDKey, PersistentDataType.STRING)) throw new IllegalStateException("Entity does not contain an instant ID Key!");

		String entityInstanceID = entityPDC.get(actorInstanceIDKey, PersistentDataType.STRING);

		UUID instanceID;
		try {
			if (entityInstanceID == null) throw new IllegalStateException("Entity Instance ID is null");
			instanceID = UUID.fromString(entityInstanceID);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException(
					"Actor entity contains an invalid instance UUID: " + entityInstanceID,
					exception
			);
		}
		return instanceID;
	}

	public ActorInstance resolveInstance(Entity entity) {
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");

		ActorDefinition definition = resolveDefinition(entity);
		if (definition == null) return null;

		UUID instanceID = resolveStoredInstanceID(entity);

		ActorInstance instance = actorInstanceRegistry.getInstance(instanceID);
		if (instance == null)
			throw new IllegalStateException("instance does not exist");
		if (!definition.getActorID().equals(instance.getActorDefinition().getActorID()))
			throw new IllegalStateException("Actor definition and actor instance do not have matching IDs");

		return instance;

	}

}

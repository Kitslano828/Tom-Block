package org.tomdang.actorframework.spawn.configuration;

import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActorSpawnPointConfigurationDefinitionRegistrar {

	private final ActorRegistry actorRegistry;
	private final ActorSpawnPointRegistry actorSpawnPointRegistry;
	private final ActorSpawnPointConfigurationConverter converter;

	public ActorSpawnPointConfigurationDefinitionRegistrar(
			ActorRegistry actorRegistry,
			ActorSpawnPointRegistry actorSpawnPointRegistry,
			ActorSpawnPointConfigurationConverter converter
	) {
		if (actorRegistry == null) throw new IllegalArgumentException("actorRegistry cannot be null");
		if (actorSpawnPointRegistry == null) throw new IllegalArgumentException("actorSpawnPointRegistry cannot be null");
		if (converter == null) throw new IllegalArgumentException("converter cannot be null");

		this.actorRegistry = actorRegistry;
		this.actorSpawnPointRegistry = actorSpawnPointRegistry;
		this.converter = converter;
	}

	public void registerDefinitions(List<ActorSpawnPointConfigurationDefinition> definitions) {
		if (definitions == null) throw new IllegalArgumentException("definitions cannot be null");

		List<ActorSpawnPoint> convertedSpawnPoints = new ArrayList<>();
		Set<String> spawnPointIDs = new HashSet<>();

		for (ActorSpawnPointConfigurationDefinition definition : definitions) {
			if (definition == null) throw new IllegalArgumentException("definition entry cannot be null");

			String spawnPointID = definition.spawnPointID();
			if (!spawnPointIDs.add(spawnPointID)) {
				throw new IllegalStateException("Duplicate spawn-point configuration: " + spawnPointID);
			}
			if (actorSpawnPointRegistry.isSpawnPointRegistered(spawnPointID)) {
				throw new IllegalStateException("Spawn point " + spawnPointID + " is already registered");
			}
			if (!actorRegistry.isActorRegistered(definition.actorID())) {
				throw new IllegalStateException(
						"Spawn point " + spawnPointID + " references an unregistered actor: " + definition.actorID()
				);
			}

			convertedSpawnPoints.add(converter.toSpawnPoint(definition));
		}

		for (ActorSpawnPoint spawnPoint : convertedSpawnPoints) {
			actorSpawnPointRegistry.registerSpawnPoint(spawnPoint);
		}
	}
}

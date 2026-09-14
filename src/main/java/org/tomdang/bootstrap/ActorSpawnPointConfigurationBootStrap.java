package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;
import org.tomdang.actorframework.spawn.configuration.ActorSpawnPointConfigurationConverter;
import org.tomdang.actorframework.spawn.configuration.ActorSpawnPointConfigurationDefinition;
import org.tomdang.actorframework.spawn.configuration.ActorSpawnPointConfigurationDefinitionRegistrar;
import org.tomdang.actorframework.spawn.configuration.ActorSpawnPointConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ActorSpawnPointConfigurationBootStrap {

	public ActorSpawnPointConfigurationBootStrap(
			TomBlock instance,
			ActorRegistry actorRegistry,
			ActorSpawnPointRegistry actorSpawnPointRegistry
	) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (actorRegistry == null) throw new IllegalArgumentException("actorRegistry cannot be null");
		if (actorSpawnPointRegistry == null) throw new IllegalArgumentException("actorSpawnPointRegistry cannot be null");

		ActorSpawnPointConfigurationLoader loader = new ActorSpawnPointConfigurationLoader();
		List<ActorSpawnPointConfigurationDefinition> definitions;
		try (InputStream configurationStream = instance.getResource("spawn-points.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain spawn-points.yml");
			}
			definitions = loader.loadDefinitions(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled spawn-points.yml resource", exception);
		}

		ActorSpawnPointConfigurationDefinitionRegistrar registrar =
				new ActorSpawnPointConfigurationDefinitionRegistrar(
						actorRegistry,
						actorSpawnPointRegistry,
						new ActorSpawnPointConfigurationConverter(instance.getServer())
				);
		registrar.registerDefinitions(definitions);
	}
}

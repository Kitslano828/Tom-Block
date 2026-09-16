package org.tomdang.bootstrap;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.tomdang.TomBlock;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.custommobframework.custommobspawn.CustomMobRespawnService;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPoint;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPointRegistry;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPointResolver;
import org.tomdang.custommobframework.configuration.CustomMobConfigurationLoader;
import org.tomdang.custommobframework.configuration.CustomMobDefinition;
import org.tomdang.custommobframework.configuration.CustomMobDefinitionRegistrar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MobBootStrap {

	@Getter
	private final CustomMobRegistry customMobRegistry;
	@Getter
	private final CustomMobResolver customMobResolver;
	@Getter
	private final CustomMobHealthService customMobHealthService;
	@Getter
	private final CustomMobRespawnService customMobRespawnService;
	@Getter
	private final CustomMobContextRegistry customMobContextRegistry;
	private final CustomMobSpawnPointRegistry customMobSpawnPointRegistry;

	public MobBootStrap(TomBlock instance, NamespacedKey customMobKey, NamespacedKey spawnPointIDKey,
						CustomItemRegistry customItemRegistry
	) {
		customMobContextRegistry = new CustomMobContextRegistry();
		CustomMobSpawner customMobSpawner = new CustomMobSpawner(customMobKey, spawnPointIDKey, customMobContextRegistry);
		customMobRegistry = new CustomMobRegistry(customMobSpawner);
		List<CustomMobDefinition> definitions;
		try (InputStream stream = instance.getResource("mobs.yml")) {
			if (stream == null) throw new IllegalStateException("TomBlock.jar does not contain mobs.yml");
			definitions = new CustomMobConfigurationLoader().loadDefinitions(
					new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled mobs.yml resource", exception);
		}
		new CustomMobDefinitionRegistrar(customMobRegistry, customItemRegistry).registerDefinitions(definitions);
		customMobHealthService = new CustomMobHealthService(customMobContextRegistry);
		customMobSpawnPointRegistry = new CustomMobSpawnPointRegistry();
		CustomMobSpawnPointResolver customMobSpawnPointResolver = new CustomMobSpawnPointResolver(spawnPointIDKey, customMobSpawnPointRegistry);
		customMobRespawnService = new CustomMobRespawnService(
				instance,
				customMobRegistry,
				customMobSpawner,
				customMobSpawnPointResolver,
				spawnPointIDKey,
				customMobContextRegistry
		);
		customMobResolver = new CustomMobResolver(customMobKey, customMobRegistry);
	}

	public void reconcileSpawnPoints() {
		for (CustomMobSpawnPoint spawnPoint : customMobSpawnPointRegistry.getAllSpawnPoints()) {
			customMobRespawnService.reconcileSpawnPoint(spawnPoint);
		}

	}

}

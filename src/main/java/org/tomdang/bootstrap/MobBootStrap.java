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
		customMobRegistry = new CustomMobRegistry(customItemRegistry, customMobSpawner);
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

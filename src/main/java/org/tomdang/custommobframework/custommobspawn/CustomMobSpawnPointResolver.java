package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomMobSpawnPointResolver {

	private final NamespacedKey customSpawnPointIDKey;
	private final CustomMobSpawnPointRegistry customMobSpawnPointRegistry;

	public CustomMobSpawnPointResolver(NamespacedKey customSpawnPointIDKey, CustomMobSpawnPointRegistry customMobSpawnPointRegistry) {
		this.customSpawnPointIDKey = customSpawnPointIDKey;
		this.customMobSpawnPointRegistry = customMobSpawnPointRegistry;
	}

	public CustomMobSpawnPoint getCustomMobSpawnPoint(Entity entity) {
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (pdc.has(customSpawnPointIDKey, PersistentDataType.STRING)) {
			String id = pdc.get(customSpawnPointIDKey, PersistentDataType.STRING);
			return customMobSpawnPointRegistry.getCustomSpawnPoint(id);
		}
		return null;
	}

}

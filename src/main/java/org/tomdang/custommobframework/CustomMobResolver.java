package org.tomdang.custommobframework;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomMobResolver {

	private final NamespacedKey customMobIDKey;
	private final CustomMobRegistry customMobRegistry;

	public CustomMobResolver (NamespacedKey customMobIDKey, CustomMobRegistry customMobRegistry) {
		this.customMobIDKey = customMobIDKey;
		this.customMobRegistry = customMobRegistry;
	}

	public CustomMob getCustomMob(Entity entity) {

		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (pdc.has(customMobIDKey, PersistentDataType.STRING)) {
			String id = pdc.get(customMobIDKey, PersistentDataType.STRING);
			return customMobRegistry.getCustomMob(id);
		}
		return null;
	}
}

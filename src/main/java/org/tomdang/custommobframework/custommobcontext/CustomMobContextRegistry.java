package org.tomdang.custommobframework.custommobcontext;

import lombok.Getter;
import org.tomdang.custommobframework.CustomMob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomMobContextRegistry {

	@Getter
	private final Map<UUID, CustomMobContext> customMobContextMap = new HashMap<>();

	public CustomMobContextRegistry() {

	}

	public void addCustomMobContextToRegistry(CustomMobContext customMobContext) {
		customMobContextMap.put(customMobContext.getMobUUID(), customMobContext);
	}

	public CustomMobContext getCustomMobContext(UUID uuid) {
		return customMobContextMap.get(uuid);
	}

	public void removeCustomMobContext(UUID uuid) {
		customMobContextMap.remove(uuid);
	}

	public void createNewCustomMobContext(UUID uuid, CustomMob customMob) {
		customMobContextMap.put(uuid, new CustomMobContext(uuid, customMob.getMaxHealth(), customMob));
	}
}

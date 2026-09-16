package org.tomdang.custommobframework;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.tomdang.customitemframework.CustomItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomMobRegistry {
	private final Map<String, CustomMob> customMobMap = new HashMap<>();
	private final CustomMobSpawner customMobSpawner;

	public CustomMobRegistry(CustomMobSpawner customMobSpawner) {
		if (customMobSpawner == null) throw new IllegalArgumentException("customMobSpawner cannot be null");
		this.customMobSpawner = customMobSpawner;
	}

	public void addDropToCustomMob(CustomMob customMob, CustomItem customItem, int amount,  double chance) {
		customMob.addMobDrops(customItem, amount, chance);
	}

	public boolean isACustomMob(String id) {
		return  customMobMap.containsKey(id);
	}

	public void addMobToRegistry(CustomMob mob) {
		customMobMap.put(mob.getId(), mob);
	}

	public Entity getCustomMobAsMob(CustomMob mob, Location location) {
		return customMobSpawner.createCustomMob(mob, location, null);
	}

	public CustomMob getCustomMob(String id) {
		return customMobMap.get(id);
	}

	public List<String> getCustomMobsAsList() {
		return new ArrayList<>(customMobMap.keySet());
	}
}

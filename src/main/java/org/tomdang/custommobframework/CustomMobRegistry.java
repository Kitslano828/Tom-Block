package org.tomdang.custommobframework;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitems.RottenFlesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomMobRegistry {
	RottenFlesh rottenFlesh = new RottenFlesh("ROTTEN_FLESH", Material.ROTTEN_FLESH, "TEST ROTTEN FLESH", Rarity.COMMON, ItemCategory.MOB_DROP);

	CustomItemRegistry customItemRegistry;
	private final Map<String, CustomMob> customMobMap = new HashMap<>();
	CustomMobSpawner customMobSpawner;

	public CustomMobRegistry(CustomItemRegistry customItemRegistry, CustomMobSpawner customMobSpawner) {
		this.customItemRegistry = customItemRegistry;
		this.customMobSpawner = customMobSpawner;

		createNewCustomMob("TRAINING_ZOMBIE", EntityType.ZOMBIE, "Training Zombie", 100.0, 20, MobType.COMMON_MOB, 5);

		addDropToCustomMob(customMobMap.get("TRAINING_ZOMBIE"), rottenFlesh, 2, 100.0);
	}

	public void createNewCustomMob(String id, EntityType entityType, String name,
								   double maxHealth, double damage, MobType mobType, int xpAmount) {
		CustomMob customMob = new CustomMob(id, entityType, name, maxHealth, damage,mobType, xpAmount);
		customMobMap.put(id, customMob);
	}

	public void addDropToCustomMob(CustomMob customMob, CustomItem customItem, int amount,  double chance) {
		customMob.addMobDrops(customItem, amount, chance);
		customItemRegistry.addItemToRegistry(customItem);
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

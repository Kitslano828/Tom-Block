package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;

public class 	CustomMobRespawnService {

	CustomMobRegistry customMobRegistry;
	private final TomBlock instance;
	CustomMobSpawner customMobSpawner;
	CustomMobSpawnPointResolver customMobSpawnPointResolver;
	NamespacedKey spawnPointIDKey;
	private final CustomMobContextRegistry customMobContextRegistry;

	public CustomMobRespawnService(TomBlock instance, CustomMobRegistry customMobRegistry ,
								   CustomMobSpawner customMobSpawner, CustomMobSpawnPointResolver customMobSpawnPointResolver,
									NamespacedKey spawnPointIDKey, CustomMobContextRegistry customMobContextRegistry
	) {
		this.instance = instance;
		this.customMobRegistry = customMobRegistry;
		this.customMobSpawner = customMobSpawner;
		this.customMobSpawnPointResolver = customMobSpawnPointResolver;
		this.spawnPointIDKey = spawnPointIDKey;
		this.customMobContextRegistry = customMobContextRegistry;
	}

	public void handleMobDeath(EntityDeathEvent event) {
		customMobContextRegistry.removeCustomMobContext(event.getEntity().getUniqueId());
		CustomMobSpawnPoint customMobSpawnPoint = customMobSpawnPointResolver.getCustomMobSpawnPoint(event.getEntity());
		if (customMobSpawnPoint == null) {
			return;
		}
		customMobSpawnPoint.setOccupied(false);
		Bukkit.getScheduler().runTaskLater( instance, () -> {
			reconcileSpawnPoint(customMobSpawnPoint);
		},customMobSpawnPoint.getRespawnDelayInTicks());
	}

	public void spawnMob(CustomMobSpawnPoint customMobSpawnpoint) {

		World world = customMobSpawnpoint.getLocation().getWorld();
		if (customMobSpawnpoint.isOccupied()) {
			return;
		}
		CustomMob mob = customMobRegistry.getCustomMob(customMobSpawnpoint.getCustomMobID());
		if (mob == null) {
			return;
		}
		Entity spawned = customMobSpawner.createCustomMob(mob, customMobSpawnpoint.getLocation(), customMobSpawnpoint);
		customMobSpawnpoint.setOccupied(spawned != null);
	}

	public void reconcileSpawnPoint(CustomMobSpawnPoint spawnPoint) {
		World world = spawnPoint.getLocation().getWorld();
		boolean foundExistingMob = false;

		if (world == null) {
			return;
		}
		String spawnPointID = spawnPoint.getSpawnPointID();
		if (spawnPointID == null) return;
		CustomMob customMob = customMobRegistry.getCustomMob(spawnPoint.getCustomMobID());
		if (customMob == null) throw new IllegalStateException("Unknown custom mob " + spawnPoint.getCustomMobID());
		spawnPoint.getLocation().getChunk().load();

		for (Entity entity : world.getEntities()) {
			String id = entity.getPersistentDataContainer().get(spawnPointIDKey, PersistentDataType.STRING);
			if (!spawnPointID.equals(id) || !entity.isValid() || entity.isDead()) continue;

			if (foundExistingMob) {
				customMobContextRegistry.removeCustomMobContext(entity.getUniqueId());
				entity.remove();
				continue;
			}

			foundExistingMob = true;
			customMobSpawner.applyEntityBehavior(customMob, entity);
			// Health is recovered from the entity's saved data on its next hit.
		}

		spawnPoint.setOccupied(foundExistingMob);
		if (!foundExistingMob) {
			spawnMob(spawnPoint);
		}
	}

}

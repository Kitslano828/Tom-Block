package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.custommobcontext.CustomMobContext;
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
			spawnMob(customMobSpawnPoint);
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
		customMobSpawner.createCustomMob(mob, customMobSpawnpoint.getLocation(), customMobSpawnpoint);

		customMobSpawnpoint.setOccupied(true);
	}

	public void reconcileSpawnPoint(CustomMobSpawnPoint spawnPoint) {
		World world = spawnPoint.getLocation().getWorld();
		boolean foundExistingMob = false;

		if (world == null) {
			return;
		}
		spawnPoint.getLocation().getChunk().load();

		for (Entity entity : world.getEntities()) {
			String id = entity.getPersistentDataContainer().get(spawnPointIDKey, PersistentDataType.STRING);
			CustomMobContext context = customMobContextRegistry.getCustomMobContext(entity.getUniqueId());

			if (spawnPoint.getSpawnPointID() == null) {
				continue;
			} else {
				if(spawnPoint.getSpawnPointID().equals(id) && entity.isValid() && !entity.isDead()) {
					if (context != null) {
						if (!foundExistingMob) {
							foundExistingMob = true;
						} else {
							entity.remove();
						}
					} else {
						customMobContextRegistry.createNewCustomMobContext(entity.getUniqueId(), customMobRegistry.getCustomMob(spawnPoint.getCustomMobID()));
						foundExistingMob = true;
					}

				}
			}

		}

		spawnPoint.setOccupied(foundExistingMob);
		if (!foundExistingMob) {
			spawnMob(spawnPoint);
		}
	}

	private boolean spawnPointHasLivingEntity(CustomMobSpawnPoint spawnPoint) {

		World world = spawnPoint.getLocation().getWorld();
		if (world == null) return false;
		Chunk chunk = world.getChunkAt(spawnPoint.getLocation());
		String spawnPointID = spawnPoint.getSpawnPointID();
		if (spawnPointID == null) return false;

		for (Entity entity : chunk.getEntities()) {
			String id = entity.getPersistentDataContainer().get(spawnPointIDKey, PersistentDataType.STRING);
			if (id == null) continue;
			if (id.equals(spawnPointID) && !entity.isDead() && entity.isValid()) {
				return true;
			}
		}
		return false;
	}
}
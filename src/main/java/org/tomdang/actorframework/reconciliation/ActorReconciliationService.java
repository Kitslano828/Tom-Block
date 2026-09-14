package org.tomdang.actorframework.reconciliation;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.lifecycle.ActorLifecycleService;
import org.tomdang.actorframework.resolver.ActorResolver;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPointService;

public class ActorReconciliationService {

	private final ActorResolver actorResolver;
	private final ActorLifecycleService actorLifecycleService;
	private final ActorSpawnPointRegistry actorSpawnPointRegistry;
	private final ActorSpawnPointService actorSpawnPointService;

	public ActorReconciliationService(ActorResolver actorResolver, ActorLifecycleService actorLifecycleService,
									  ActorSpawnPointRegistry actorSpawnPointRegistry, ActorSpawnPointService actorSpawnPointService
	) {
		if (actorResolver == null) throw new IllegalArgumentException("ActorResolver cannot be null");
		if (actorLifecycleService == null) throw new IllegalArgumentException("ActorLifeCycleService cannot be null");
		if (actorSpawnPointRegistry == null) throw new IllegalArgumentException("ActorSpawnPointRegistry cannot be null");
		if (actorSpawnPointService == null) throw new IllegalArgumentException("ActorSpawnPointService cannot be null");

		this.actorResolver = actorResolver;
		this.actorLifecycleService = actorLifecycleService;
		this.actorSpawnPointRegistry = actorSpawnPointRegistry;
		this.actorSpawnPointService = actorSpawnPointService;
	}

	public boolean restoreEntity(Entity entity) {
		if (entity == null) throw new IllegalArgumentException();

		ActorDefinition definition = actorResolver.resolveDefinition(entity);
		if (definition == null) return false;

		String spawnPointID = actorResolver.resolveSpawnPointID(entity);
		if (spawnPointID == null) return false;

		ActorSpawnPoint spawnPoint = actorSpawnPointRegistry.lookupSpawnPoint(spawnPointID);
		if (spawnPoint == null) {
			throw new IllegalStateException("Actor entity references unknown spawn point " + spawnPointID);
		}

		if (!spawnPoint.getActorID().equals(definition.getActorID())) {
			throw new IllegalStateException(
					"Actor entity definition " + definition.getActorID()
							+ " does not match spawn point " + spawnPointID
							+ " definition " + spawnPoint.getActorID()
			);
		}

		ActorAudienceKey key = actorResolver.resolveAudienceKey(entity);
		if (!key.equals(spawnPoint.getAudienceKey())) {
			throw new IllegalStateException(
					"Actor entity audience does not match spawn point " + spawnPointID
			);
		}

		actorLifecycleService.restoreActor(actorResolver.resolveStoredInstanceID(entity), definition.getActorID(), key, spawnPointID, entity.getUniqueId());
		return true;
	}

	public void reconcileSpawnPoints() {
		// Fixed spawn-point actors must be loaded before the world entity scan.
		// Otherwise an existing actor in an unloaded chunk could be missed and duplicated.
		for (ActorSpawnPoint spawnPoint : actorSpawnPointRegistry.getAllSpawnPoints()) {
			spawnPoint.getLocation().getChunk().load();
		}

		// Packet-player actors are recreated on every startup. Remove any Bukkit
		// entities left behind by the former persistent presentation model first.
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (actorResolver.isManagedActorEntity(entity)) {
					entity.remove();
				}
			}
		}

		for (ActorSpawnPoint spawnPoint : actorSpawnPointRegistry.getAllSpawnPoints()) {
			actorSpawnPointService.spawnAtPoint(spawnPoint.getSpawnPointID());
		}
	}
}

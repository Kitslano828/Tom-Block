package org.tomdang.actorframework.spawn;

import org.bukkit.Location;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.lifecycle.ActorLifecycleService;

public class ActorSpawnPointService {

	private final ActorSpawnPointRegistry actorSpawnPointRegistry;
	private final ActorLifecycleService actorLifecycleService;

	public ActorSpawnPointService(ActorSpawnPointRegistry actorSpawnPointRegistry, ActorLifecycleService actorLifecycleService) {
		if (actorSpawnPointRegistry == null) throw new IllegalArgumentException("Actor spawn point registry cannot be null");
		if (actorLifecycleService == null) throw new IllegalArgumentException("Actor life cycle service cannot be null");

		this.actorSpawnPointRegistry = actorSpawnPointRegistry;
		this.actorLifecycleService = actorLifecycleService;
	}

	public ActorInstance spawnAtPoint(String spawnPointID) {
		if (!actorSpawnPointRegistry.isSpawnPointRegistered(spawnPointID)) throw new IllegalArgumentException(spawnPointID + " does not exist!");
		ActorSpawnPoint spawnPoint = actorSpawnPointRegistry.lookupSpawnPoint(spawnPointID);

		String actorID = spawnPoint.getActorID();
		ActorAudienceKey audienceKey = spawnPoint.getAudienceKey();
		Location clonedLocation = spawnPoint.getLocation();

		return actorLifecycleService.createAndSpawnActor(actorID, audienceKey, clonedLocation, spawnPointID);
	}

}

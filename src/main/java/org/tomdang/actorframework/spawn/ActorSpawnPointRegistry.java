package org.tomdang.actorframework.spawn;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ActorSpawnPointRegistry {

	private final Map<String, ActorSpawnPoint> actorSpawnPointMap = new HashMap<>();

	public void registerSpawnPoint(ActorSpawnPoint spawnPoint) {
		if (spawnPoint == null) throw new IllegalArgumentException("Spawn Point cannot be null");
		String spawnPointID = spawnPoint.getSpawnPointID();
		if (actorSpawnPointMap.containsKey(spawnPointID)) throw new IllegalStateException("Spawn point already exist");
		actorSpawnPointMap.put(spawnPointID, spawnPoint);
	}

	public ActorSpawnPoint lookupSpawnPoint(String spawnPointID) {
		if (spawnPointID == null) throw new IllegalArgumentException("Spawn point ID cannot be null");
		if (spawnPointID.isBlank()) throw new IllegalArgumentException("Spawn point ID cannot be blank");
		return actorSpawnPointMap.get(spawnPointID);
	}

	public boolean isSpawnPointRegistered(String spawnPointID) {
		if (spawnPointID == null) throw new IllegalArgumentException("Spawn point ID cannot be null");
		if (spawnPointID.isBlank()) throw new IllegalArgumentException("Spawn point ID cannot be blank");
		return actorSpawnPointMap.containsKey(spawnPointID);
	}

	public Collection<ActorSpawnPoint> getAllSpawnPoints() {
		return Collections.unmodifiableCollection(this.actorSpawnPointMap.values());
	}

}

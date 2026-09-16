package org.tomdang.custommobframework.custommobspawn;

import org.bukkit.Location;
import java.util.*;

public class CustomMobSpawnPointRegistry {

	private final Map<String,CustomMobSpawnPoint> customMobSpawnPoints = new HashMap<>();

	public CustomMobSpawnPointRegistry() { }

	public void registerSpawnPoint(String spawnPointID, Location location, String customMobID,
											   long respawnDelayInTicks) {
		CustomMobSpawnPoint customMobSpawnPoint = new CustomMobSpawnPoint(location, customMobID, respawnDelayInTicks, spawnPointID);
		customMobSpawnPoints.put(spawnPointID, customMobSpawnPoint);
	}

	public void addCustomMobSpawnPoint(CustomMobSpawnPoint customMobSpawnPoint) {
		customMobSpawnPoints.put(customMobSpawnPoint.getSpawnPointID(), customMobSpawnPoint);
	}

	public Collection<CustomMobSpawnPoint> getAllSpawnPoints() {
		return Collections.unmodifiableCollection(customMobSpawnPoints.values());
	}

	public CustomMobSpawnPoint getCustomSpawnPoint(String spawnPointID) {
		return customMobSpawnPoints.get(spawnPointID);
	}
}

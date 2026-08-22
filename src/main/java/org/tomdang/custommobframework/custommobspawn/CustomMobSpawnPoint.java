package org.tomdang.custommobframework.custommobspawn;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class CustomMobSpawnPoint {

	@Getter
	private final String spawnPointID;
	@Getter
	private final Location location;
	@Getter
	private final String customMobID;
	@Getter
	private final long respawnDelayInTicks;
	@Getter @Setter
	boolean occupied = false;

	public CustomMobSpawnPoint(Location location, String customMobID, long respawnDelayInTicks, String spawnPointID) {
		this.customMobID = customMobID;
		this.location = location;
		this.respawnDelayInTicks = respawnDelayInTicks;
		this.spawnPointID = spawnPointID;
	}

}

package org.tomdang.actorframework.spawn;

import lombok.Getter;
import org.bukkit.Location;
import org.tomdang.actorframework.audience.ActorAudienceKey;

public class ActorSpawnPoint {

	@Getter
	private final String spawnPointID;
	@Getter
	private final String actorID;
	@Getter
	private final ActorAudienceKey audienceKey;
	private final Location location;

	public ActorSpawnPoint(String spawnPointID, String actorID, ActorAudienceKey audienceKey, Location location) {
		if (spawnPointID == null) throw new IllegalArgumentException("Spawn Point ID cannot be null");
		if (spawnPointID.isBlank()) throw new IllegalArgumentException("Spawn Point ID cannot be blank");
		if (actorID == null) throw new IllegalArgumentException("Actor ID cannot be null");
		if (actorID.isBlank()) throw new IllegalArgumentException("Actor ID cannot be blank");
		if (audienceKey == null) throw new IllegalArgumentException("Audience Key cannot be null");
		if (location == null) throw new IllegalArgumentException("Location cannot be null");
		if (location.getWorld() == null) throw new IllegalArgumentException("Location's world cannot be null");

		this.spawnPointID = spawnPointID;
		this.actorID = actorID;
		this.audienceKey = audienceKey;
		this.location = location.clone();
	}

	public Location getLocation() {
		return location.clone();
	}
}

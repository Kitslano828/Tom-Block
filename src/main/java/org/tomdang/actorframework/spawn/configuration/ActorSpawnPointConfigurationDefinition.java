package org.tomdang.actorframework.spawn.configuration;

import org.tomdang.actorframework.audience.ActorAudienceScope;

import java.util.UUID;

public record ActorSpawnPointConfigurationDefinition(String spawnPointID, String actorID, ActorAudienceScope audienceScope, UUID audienceID, String worldName, double x, double y, double z, float yaw, float pitch) {

	public ActorSpawnPointConfigurationDefinition {
		if (spawnPointID == null || spawnPointID.isBlank()) {
			throw new IllegalArgumentException("spawnPointID cannot be null or blank");
		}
		if (actorID == null || actorID.isBlank()) {
			throw new IllegalArgumentException("actorID cannot be null or blank");
		}
		if (worldName == null || worldName.isBlank()) {
			throw new IllegalArgumentException("worldName cannot be null or blank");
		}
		if (audienceScope == null) {
			throw new IllegalArgumentException("audienceScope cannot be null");
		}

		if (audienceScope == ActorAudienceScope.GLOBAL) {
			if (audienceID != null) {
				throw new IllegalArgumentException("GLOBAL audience scope must have a null audienceID");
			}
		} else {
			if (audienceID == null) {
				throw new IllegalArgumentException("Non-global audience scope requires an audienceID");
			}
		}

		if (!Double.isFinite(x)) {
			throw new IllegalArgumentException("x coordinate must be finite (got: " + x + ")");
		}
		if (!Double.isFinite(y)) {
			throw new IllegalArgumentException("y coordinate must be finite (got: " + y + ")");
		}
		if (!Double.isFinite(z)) {
			throw new IllegalArgumentException("z coordinate must be finite (got: " + z + ")");
		}

		if (!Float.isFinite(yaw)) {
			throw new IllegalArgumentException("yaw rotation must be finite (got: " + yaw + ")");
		}
		if (!Float.isFinite(pitch)) {
			throw new IllegalArgumentException("pitch rotation must be finite (got: " + pitch + ")");
		}
	}

}

package org.tomdang.actorframework.spawn.configuration;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;

public class ActorSpawnPointConfigurationConverter {

	private final Server server;

	public ActorSpawnPointConfigurationConverter(Server server) {
		if (server == null) throw new IllegalArgumentException("server cannot be null");
		this.server = server;
	}

	public ActorSpawnPoint toSpawnPoint(ActorSpawnPointConfigurationDefinition definition) {
		if (definition == null) throw new IllegalArgumentException("definition cannot be null");

		World world = server.getWorld(definition.worldName());
		if (world == null) {
			throw new IllegalStateException(
					"Spawn point " + definition.spawnPointID() + " references an unloaded or unknown world: " + definition.worldName()
			);
		}

		ActorAudienceKey audienceKey = switch (definition.audienceScope()) {
			case GLOBAL -> ActorAudienceKey.global();
			case PLAYER -> ActorAudienceKey.player(definition.audienceID());
			case PARTY -> ActorAudienceKey.party(definition.audienceID());
			case ENCOUNTER -> ActorAudienceKey.encounter(definition.audienceID());
		};

		Location location = new Location(
				world,
				definition.x(),
				definition.y(),
				definition.z(),
				definition.yaw(),
				definition.pitch()
		);

		return new ActorSpawnPoint(
				definition.spawnPointID(),
				definition.actorID(),
				audienceKey,
				location
		);
	}
}

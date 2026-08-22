package org.tomdang.actorframework.audience;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class ActorAudienceResolver {

	private final Server server;

	public ActorAudienceResolver(Server server) {


		this.server = server;
	}


	public Collection<Player> resolvePlayers(ActorAudienceKey key) {

		if (key == ActorAudienceKey.global()) return List.of();

	}
}

package org.tomdang.actorframework.audience;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActorAudienceResolver {

	private final Server server;

	public ActorAudienceResolver(Server server) {
		if (server == null) throw new IllegalArgumentException("Server cannot be null");

		this.server = server;
	}


	public Collection<Player> resolvePlayers(ActorAudienceKey key) {
		if (key == null) throw new IllegalArgumentException("Key cannot be null");
		if (key.scope() == ActorAudienceScope.PARTY || key.scope() == ActorAudienceScope.ENCOUNTER) throw new UnsupportedOperationException("Party and Encounter are yet to be implemented");

		List<Player> players = new ArrayList<>();

		for (Player player : server.getOnlinePlayers()) {
			if (isMember(player, key)) players.add(player);
		}

		return List.copyOf(players);

	}

	public boolean isMember(Player player, ActorAudienceKey key) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (key == null) throw new IllegalArgumentException("Key cannot be null");

		ActorAudienceScope scope = key.scope();
		return switch (scope) {
			case GLOBAL -> {yield  true;}
			case PLAYER -> { yield  player.getUniqueId().equals(key.audienceID());}
			case PARTY -> {throw new UnsupportedOperationException("Party has yet to be implemented");}
			case ENCOUNTER -> {throw new UnsupportedOperationException("Encounter has yet to be implemented");}
		};
	}
}

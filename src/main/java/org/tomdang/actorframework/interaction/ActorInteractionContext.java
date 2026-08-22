package org.tomdang.actorframework.interaction;

import org.bukkit.entity.Player;
import org.tomdang.actorframework.instance.ActorInstance;

public record ActorInteractionContext(Player player, ActorInstance instance) {

	public ActorInteractionContext {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
	}

}

package org.tomdang.playernpc.integration.actor;

import org.bukkit.entity.Player;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.interaction.ActorInteractionService;
import org.tomdang.playernpc.runtime.PlayerNPC;
import org.tomdang.playernpc.runtime.PlayerNpcRegistry;
import org.tomdang.playernpc.runtime.PlayerNpcVisibilityRegistry;

import java.util.UUID;

public class PlayerNpcActorInteractionService {

	private final PlayerNpcRegistry playerNpcRegistry;
	private final PlayerNpcVisibilityRegistry playerNpcVisibilityRegistry;
	private final PlayerNpcActorResolver playerNpcActorResolver;
	private final ActorInteractionService actorInteractionService;

	public PlayerNpcActorInteractionService(PlayerNpcRegistry playerNpcRegistry, PlayerNpcVisibilityRegistry playerNpcVisibilityRegistry, PlayerNpcActorResolver playerNpcActorResolver, ActorInteractionService actorInteractionService) {
		if (playerNpcRegistry == null) throw new IllegalArgumentException("playerNpcRegistry cannot be null");
		if (playerNpcVisibilityRegistry == null) throw new IllegalArgumentException("playerNpcVisibilityRegistry cannot be null");
		if (playerNpcActorResolver == null) throw new IllegalArgumentException("playerNpcActorResolver cannot be null");
		if (actorInteractionService == null) throw new IllegalArgumentException("actorInteractionService cannot be null");

		this.playerNpcRegistry = playerNpcRegistry;
		this.playerNpcVisibilityRegistry = playerNpcVisibilityRegistry;
		this.playerNpcActorResolver = playerNpcActorResolver;
		this.actorInteractionService = actorInteractionService;
	}

	public boolean interact(Player player, int entityID) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");

		PlayerNPC npc = playerNpcRegistry.getByEntityID(entityID);
		if (npc == null) return false;
		UUID profileUUID = npc.getProfileUUID();

		if (!playerNpcVisibilityRegistry.isVisibleTo(profileUUID, player.getUniqueId())) return false;

		ActorInstance instance = playerNpcActorResolver.resolveByProfileID(profileUUID);
		if (instance == null) return false;

		return actorInteractionService.interact(player, instance);
	}

}

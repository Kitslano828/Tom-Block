package org.tomdang.playernpc.integration.actor;

import org.bukkit.entity.Player;
import org.tomdang.actorframework.audience.ActorAudienceResolver;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.runtime.PlayerNPC;
import org.tomdang.playernpc.runtime.PlayerNpcRegistry;

import java.util.UUID;

public class PlayerNpcActorVisibilityService {

	private final PlayerNpcRegistry playerNpcRegistry;
	private final PlayerNpcActorResolver playerNpcActorResolver;
	private final ActorAudienceResolver actorAudienceResolver;
	private final PlayerNpcLifecycleService playerNpcLifecycleService;

	public PlayerNpcActorVisibilityService(PlayerNpcRegistry playerNpcRegistry, PlayerNpcActorResolver playerNpcActorResolver, ActorAudienceResolver actorAudienceResolver, PlayerNpcLifecycleService playerNpcLifecycleService) {
		if (playerNpcRegistry == null) throw new IllegalArgumentException("playerNpcRegistry cannot be null");
		if (playerNpcActorResolver == null) throw new IllegalArgumentException("playerNpcActorResolver cannot be null");
		if (actorAudienceResolver == null) throw new IllegalArgumentException("actorAudienceResolver cannot be null");
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("playerNpcLifecycleService cannot be null");


		this.playerNpcRegistry = playerNpcRegistry;
		this.playerNpcActorResolver = playerNpcActorResolver;
		this.actorAudienceResolver = actorAudienceResolver;
		this.playerNpcLifecycleService = playerNpcLifecycleService;
	}

	public void synchronizeViewer(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");

		for (PlayerNPC playerNPC : playerNpcRegistry.getRegisteredNpcs()) {

			UUID npcUUID = playerNPC.getProfileUUID();
			ActorInstance instance = playerNpcActorResolver.resolveByProfileID(npcUUID);
			if (instance == null) continue;
			if (actorAudienceResolver.isMember(player, instance.getAudienceKey())) {
				playerNpcLifecycleService.showToViewer(player, npcUUID);
			} else {
				playerNpcLifecycleService.hideFromViewer(player, npcUUID);
			}

		}


	}

}

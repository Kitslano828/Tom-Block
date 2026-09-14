package org.tomdang.playernpc.integration.actor;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.audience.ActorAudienceResolver;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentation;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.runtime.PlayerNPC;
import org.tomdang.playernpc.runtime.PlayerNpcRegistry;

import java.util.UUID;

public class PlayerNpcActorVisibilityService {

	private final PlayerNpcRegistry playerNpcRegistry;
	private final PlayerNpcActorResolver playerNpcActorResolver;
	private final ActorAudienceResolver actorAudienceResolver;
	private final PlayerNpcLifecycleService playerNpcLifecycleService;
	private final ActorNameplatePresentation actorNameplatePresentation;

	public PlayerNpcActorVisibilityService(PlayerNpcRegistry playerNpcRegistry, PlayerNpcActorResolver playerNpcActorResolver, ActorAudienceResolver actorAudienceResolver, PlayerNpcLifecycleService playerNpcLifecycleService, ActorNameplatePresentation actorNameplatePresentation) {
		if (playerNpcRegistry == null) throw new IllegalArgumentException("playerNpcRegistry cannot be null");
		if (playerNpcActorResolver == null) throw new IllegalArgumentException("playerNpcActorResolver cannot be null");
		if (actorAudienceResolver == null) throw new IllegalArgumentException("actorAudienceResolver cannot be null");
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("playerNpcLifecycleService cannot be null");
		if (actorNameplatePresentation == null) throw new IllegalArgumentException("actorNameplatePresentation cannot be null");


		this.playerNpcRegistry = playerNpcRegistry;
		this.playerNpcActorResolver = playerNpcActorResolver;
		this.actorAudienceResolver = actorAudienceResolver;
		this.playerNpcLifecycleService = playerNpcLifecycleService;
		this.actorNameplatePresentation = actorNameplatePresentation;
	}

	public void synchronizeViewer(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");

		for (PlayerNPC playerNPC : playerNpcRegistry.getRegisteredNpcs()) {

			UUID npcUUID = playerNPC.getProfileUUID();
			ActorInstance instance = playerNpcActorResolver.resolveByProfileID(npcUUID);
			if (instance == null) continue;
			if (actorAudienceResolver.isMember(player, instance.getAudienceKey())) {
				boolean bodyWasShown = playerNpcLifecycleService.showToViewer(player, npcUUID);
				try {
					Location npcLocation = playerNpcLifecycleService.getNpcLocation(npcUUID);
					actorNameplatePresentation.showToViewer(player, instance, npcLocation, false);
				} catch (RuntimeException exception) {
					if (bodyWasShown) {
						try {
							playerNpcLifecycleService.hideFromViewer(player, npcUUID);
						} catch (RuntimeException cleanupException) {
							exception.addSuppressed(cleanupException);
						}
					}
					throw exception;
				}
			} else {
				actorNameplatePresentation.hideFromViewer(player, instance.getInstanceID());
				playerNpcLifecycleService.hideFromViewer(player, npcUUID);
			}

		}


	}

}

package org.tomdang.playernpc.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorVisibilityService;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentation;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.nms.NmsPlayerNpcInteractionInterceptor;

import java.util.UUID;

public class PlayerNpcConnectionListener implements Listener {

	private final PlayerNpcLifecycleService playerNpcLifecycleService;
	private final PlayerNpcActorVisibilityService playerNpcActorVisibilityService;
	private final NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor;
	private final ActorNameplatePresentation actorNameplatePresentation;

	public PlayerNpcConnectionListener(PlayerNpcLifecycleService playerNpcLifecycleService, PlayerNpcActorVisibilityService playerNpcActorVisibilityService, NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor, ActorNameplatePresentation actorNameplatePresentation) {
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("Player NPC lifecycle service cannot be null");
		if (playerNpcActorVisibilityService == null) throw new IllegalArgumentException("playerNpcActorVisibilityService cannot be null");
		if (nmsPlayerNpcInteractionInterceptor == null) throw new IllegalArgumentException("nmsPlayerNpcInteractionInterceptor cannot be null");
		if (actorNameplatePresentation == null) throw new IllegalArgumentException("actorNameplatePresentation cannot be null");

		this.playerNpcLifecycleService = playerNpcLifecycleService;
		this.playerNpcActorVisibilityService = playerNpcActorVisibilityService;
		this.nmsPlayerNpcInteractionInterceptor = nmsPlayerNpcInteractionInterceptor;
		this.actorNameplatePresentation = actorNameplatePresentation;
	}

	@EventHandler
	public void playerNpcJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		nmsPlayerNpcInteractionInterceptor.install(player);
		playerNpcActorVisibilityService.synchronizeViewer(player);
	}

	@EventHandler
	public void playerNpcQuitEvent(PlayerQuitEvent event) {

		Player player = event.getPlayer();
		UUID playerUUID = player.getUniqueId();
		nmsPlayerNpcInteractionInterceptor.remove(player);
		actorNameplatePresentation.clearViewer(playerUUID);
		playerNpcLifecycleService.clearViewerVisibility(playerUUID);

	}

}

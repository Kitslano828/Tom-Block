package org.tomdang.playernpc.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.playernpc.integration.actor.PlayerNpcActorVisibilityService;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;
import org.tomdang.playernpc.nms.NmsPlayerNpcInteractionInterceptor;

import java.util.UUID;

public class PlayerNpcConnectionListener implements Listener {

	private final PlayerNpcLifecycleService playerNpcLifecycleService;
	private final PlayerNpcActorVisibilityService playerNpcActorVisibilityService;
	private final NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor;

	public PlayerNpcConnectionListener(PlayerNpcLifecycleService playerNpcLifecycleService, PlayerNpcActorVisibilityService playerNpcActorVisibilityService, NmsPlayerNpcInteractionInterceptor nmsPlayerNpcInteractionInterceptor) {
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("Player NPC lifecycle service cannot be null");
		if (playerNpcActorVisibilityService == null) throw new IllegalArgumentException("playerNpcActorVisibilityService cannot be null");
		if (nmsPlayerNpcInteractionInterceptor == null) throw new IllegalArgumentException("nmsPlayerNpcInteractionInterceptor cannot be null");

		this.playerNpcLifecycleService = playerNpcLifecycleService;
		this.playerNpcActorVisibilityService = playerNpcActorVisibilityService;
		this.nmsPlayerNpcInteractionInterceptor = nmsPlayerNpcInteractionInterceptor;
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
		playerNpcLifecycleService.clearViewerVisibility(playerUUID);

	}

}

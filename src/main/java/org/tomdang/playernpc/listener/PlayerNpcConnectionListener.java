package org.tomdang.playernpc.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;

import java.util.UUID;

public class PlayerNpcConnectionListener implements Listener {

	private final PlayerNpcLifecycleService playerNpcLifecycleService;

	public PlayerNpcConnectionListener(PlayerNpcLifecycleService playerNpcLifecycleService) {
		if (playerNpcLifecycleService == null) throw new IllegalArgumentException("Player NPC lifecycle service cannot be null");

		this.playerNpcLifecycleService = playerNpcLifecycleService;
	}

	@EventHandler
	public void playerNpcQuitEvent(PlayerQuitEvent event) {

		Player player = event.getPlayer();
		UUID playerUUID = player.getUniqueId();
		playerNpcLifecycleService.clearViewerVisibility(playerUUID);

	}

}

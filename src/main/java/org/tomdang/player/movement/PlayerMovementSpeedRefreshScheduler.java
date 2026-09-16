package org.tomdang.player.movement;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerMovementSpeedRefreshScheduler {
	private final Plugin plugin;
	private final PlayerMovementSpeedService speedService;
	private final Set<UUID> pendingPlayers = new HashSet<>();

	public PlayerMovementSpeedRefreshScheduler(Plugin plugin, PlayerMovementSpeedService speedService) {
		if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
		if (speedService == null) throw new IllegalArgumentException("speedService cannot be null");
		this.plugin = plugin;
		this.speedService = speedService;
	}

	public void requestRefresh(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		UUID playerId = player.getUniqueId();
		if (!pendingPlayers.add(playerId)) return;
		try {
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				pendingPlayers.remove(playerId);
				if (player.isOnline()) speedService.apply(player);
			});
		} catch (RuntimeException exception) {
			pendingPlayers.remove(playerId);
			throw exception;
		}
	}
}

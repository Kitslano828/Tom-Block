package org.tomdang.customitemframework.refresh;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerItemRefreshScheduler {

	private final Plugin plugin;
	private final PlayerInventoryItemRefreshService refreshService;
	private final Set<UUID> pendingPlayers = new HashSet<>();

	public PlayerItemRefreshScheduler(Plugin plugin, PlayerInventoryItemRefreshService refreshService) {
		if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
		if (refreshService == null) throw new IllegalArgumentException("refreshService cannot be null");
		this.plugin = plugin;
		this.refreshService = refreshService;
	}

	public void requestRefresh(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		UUID playerId = player.getUniqueId();
		if (!pendingPlayers.add(playerId)) return;

		try {
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				pendingPlayers.remove(playerId);
				if (player.isOnline()) refreshService.refresh(player);
			});
		} catch (RuntimeException exception) {
			pendingPlayers.remove(playerId);
			throw exception;
		}
	}
}

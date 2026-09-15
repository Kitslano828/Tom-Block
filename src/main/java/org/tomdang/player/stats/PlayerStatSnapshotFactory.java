package org.tomdang.player.stats;

import org.bukkit.entity.Player;
import org.tomdang.player.playerresource.PlayerStatsService;

public final class PlayerStatSnapshotFactory {

	private final PlayerStatsService playerStatsService;

	public PlayerStatSnapshotFactory(PlayerStatsService playerStatsService) {
		if (playerStatsService == null) throw new IllegalArgumentException("playerStatsService cannot be null");
		this.playerStatsService = playerStatsService;
	}

	public PlayerStatSnapshot create(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		return playerStatsService.evaluate(player).getSnapshot();
	}
}

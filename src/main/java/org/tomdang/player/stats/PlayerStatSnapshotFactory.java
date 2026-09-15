package org.tomdang.player.stats;

import org.bukkit.entity.Player;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.EnumMap;

public final class PlayerStatSnapshotFactory {

	private final PlayerStatsService playerStatsService;

	public PlayerStatSnapshotFactory(PlayerStatsService playerStatsService) {
		if (playerStatsService == null) throw new IllegalArgumentException("playerStatsService cannot be null");
		this.playerStatsService = playerStatsService;
	}

	public PlayerStatSnapshot create(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		EnumMap<PlayerStatType, Double> effectiveStats = new EnumMap<>(PlayerStatType.class);
		for (PlayerStatType statType : PlayerStatType.values()) {
			effectiveStats.put(statType, playerStatsService.getTotalStat(player, statType));
		}
		return new PlayerStatSnapshot(effectiveStats);
	}
}

package org.tomdang.player.movement;

import org.bukkit.entity.Player;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

public final class PlayerMovementSpeedService {
	private final PlayerStatsService playerStatsService;
	private final PlayerMovementSpeedCalculator calculator;

	public PlayerMovementSpeedService(PlayerStatsService playerStatsService, PlayerMovementSpeedCalculator calculator) {
		if (playerStatsService == null) throw new IllegalArgumentException("playerStatsService cannot be null");
		if (calculator == null) throw new IllegalArgumentException("calculator cannot be null");
		this.playerStatsService = playerStatsService;
		this.calculator = calculator;
	}

	public float apply(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		float walkSpeed = calculator.calculate(playerStatsService.getTotalStat(player, PlayerStatType.SPEED));
		player.setWalkSpeed(walkSpeed);
		return walkSpeed;
	}

	public void reset(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		player.setWalkSpeed(calculator.defaultWalkSpeed());
	}
}

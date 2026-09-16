package org.tomdang.player.movement;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerMovementSpeedServiceTest {
	@Test
	void appliesEffectiveSpeedAndCanRestoreDefault() {
		PlayerStatsService stats = mock(PlayerStatsService.class);
		PlayerMovementSpeedCalculator calculator = new PlayerMovementSpeedCalculator(
				new PlayerMovementSpeedSettings(100, 0.2, 0, 1));
		PlayerMovementSpeedService service = new PlayerMovementSpeedService(stats, calculator);
		Player player = mock(Player.class);
		when(stats.getTotalStat(player, PlayerStatType.SPEED)).thenReturn(250.0);

		float applied = service.apply(player);

		assertEquals(0.5, applied, 0.000001);
		verify(player).setWalkSpeed(0.5f);
		service.reset(player);
		verify(player).setWalkSpeed(0.2f);
	}

	@Test
	void rejectsInvalidDependenciesAndPlayers() {
		PlayerStatsService stats = mock(PlayerStatsService.class);
		PlayerMovementSpeedCalculator calculator = new PlayerMovementSpeedCalculator(
				new PlayerMovementSpeedSettings(100, 0.2, 0, 1));
		assertThrows(IllegalArgumentException.class, () -> new PlayerMovementSpeedService(null, calculator));
		assertThrows(IllegalArgumentException.class, () -> new PlayerMovementSpeedService(stats, null));
		PlayerMovementSpeedService service = new PlayerMovementSpeedService(stats, calculator);
		assertThrows(IllegalArgumentException.class, () -> service.apply(null));
		assertThrows(IllegalArgumentException.class, () -> service.reset(null));
	}
}

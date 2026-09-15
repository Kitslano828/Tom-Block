package org.tomdang.player.stats;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.playerresource.PlayerStatsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerStatSnapshotFactoryTest {

	@Test
	void capturesEveryEffectiveStatExactlyOnce() {
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		Player player = mock(Player.class);
		PlayerStatSnapshotFactory factory = new PlayerStatSnapshotFactory(statsService);

		for (PlayerStatType statType : PlayerStatType.values()) {
			double effectiveValue = statType.ordinal() + 10.5;
			when(statsService.getTotalStat(player, statType)).thenReturn(effectiveValue);
		}

		PlayerStatSnapshot snapshot = factory.create(player);

		assertEquals(PlayerStatType.values().length, snapshot.asMap().size());
		for (PlayerStatType statType : PlayerStatType.values()) {
			assertEquals(statType.ordinal() + 10.5, snapshot.get(statType), 0.000001);
			verify(statsService).getTotalStat(player, statType);
		}
	}

	@Test
	void invalidDependencyAndPlayerAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatSnapshotFactory(null));
		PlayerStatSnapshotFactory factory = new PlayerStatSnapshotFactory(mock(PlayerStatsService.class));
		assertThrows(IllegalArgumentException.class, () -> factory.create(null));
	}
}

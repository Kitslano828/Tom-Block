package org.tomdang.player.stats;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.evaluation.PlayerStatEvaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerStatSnapshotFactoryTest {

	@Test
	void returnsTheSnapshotFromOneCompleteEvaluation() {
		PlayerStatsService statsService = mock(PlayerStatsService.class);
		Player player = mock(Player.class);
		PlayerStatSnapshotFactory factory = new PlayerStatSnapshotFactory(statsService);
		PlayerStatEvaluation evaluation = mock(PlayerStatEvaluation.class);
		PlayerStatSnapshot expected = PlayerStatSnapshot.defaults();
		when(statsService.evaluate(player)).thenReturn(evaluation);
		when(evaluation.getSnapshot()).thenReturn(expected);

		PlayerStatSnapshot snapshot = factory.create(player);

		assertEquals(expected, snapshot);
		verify(statsService).evaluate(player);
	}

	@Test
	void invalidDependencyAndPlayerAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatSnapshotFactory(null));
		PlayerStatSnapshotFactory factory = new PlayerStatSnapshotFactory(mock(PlayerStatsService.class));
		assertThrows(IllegalArgumentException.class, () -> factory.create(null));
	}
}

package org.tomdang.player.stats.evaluation;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatBreakdownTest {

	@Test
	void defensivelyCopiesContributions() {
		List<PlayerStatContribution> source = new ArrayList<>();
		source.add(contribution(PlayerStatType.DEFENSE));
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(PlayerStatType.DEFENSE, 10, source, 15);

		source.clear();

		assertEquals(1, breakdown.contributions().size());
		assertThrows(UnsupportedOperationException.class, () -> breakdown.contributions().clear());
	}

	@Test
	void rejectsMismatchedAndInvalidValues() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatBreakdown(null, 0, List.of(), 0));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatBreakdown(PlayerStatType.DEFENSE, Double.NaN, List.of(), 0));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatBreakdown(PlayerStatType.DEFENSE, 0, null, 0));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatBreakdown(
				PlayerStatType.DEFENSE, 0, List.of(contribution(PlayerStatType.STRENGTH)), 0));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatBreakdown(PlayerStatType.DEFENSE, 0, List.of(), Double.NaN));
	}

	private PlayerStatContribution contribution(PlayerStatType statType) {
		return new PlayerStatContribution(statType, PlayerStatContributionSource.OTHER, "source", "Source", 5);
	}
}

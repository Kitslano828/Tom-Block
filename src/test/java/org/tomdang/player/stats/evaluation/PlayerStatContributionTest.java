package org.tomdang.player.stats.evaluation;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatContributionTest {

	@Test
	void retainsStructuredSourceInformation() {
		PlayerStatContribution contribution = new PlayerStatContribution(
				PlayerStatType.MAX_HEALTH,
				PlayerStatContributionSource.ARMOR,
				"equipment:armor:feet:RABBIT_BOOTS:maxHealth",
				"Rabbit Boots",
				300
		);

		assertEquals(PlayerStatType.MAX_HEALTH, contribution.statType());
		assertEquals(PlayerStatContributionSource.ARMOR, contribution.source());
		assertEquals("Rabbit Boots", contribution.displayName());
		assertEquals(300, contribution.amount(), 0.000001);
	}

	@Test
	void invalidValuesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> contribution(null, PlayerStatContributionSource.OTHER, "id", "Name", 1));
		assertThrows(IllegalArgumentException.class, () -> contribution(PlayerStatType.DEFENSE, null, "id", "Name", 1));
		assertThrows(IllegalArgumentException.class, () -> contribution(PlayerStatType.DEFENSE, PlayerStatContributionSource.OTHER, " ", "Name", 1));
		assertThrows(IllegalArgumentException.class, () -> contribution(PlayerStatType.DEFENSE, PlayerStatContributionSource.OTHER, "id", " ", 1));
		assertThrows(IllegalArgumentException.class, () -> contribution(PlayerStatType.DEFENSE, PlayerStatContributionSource.OTHER, "id", "Name", Double.NaN));
	}

	private PlayerStatContribution contribution(PlayerStatType statType, PlayerStatContributionSource source,
	                                            String sourceId, String displayName, double amount) {
		return new PlayerStatContribution(statType, source, sourceId, displayName, amount);
	}
}

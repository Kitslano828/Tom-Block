package org.tomdang.player.stats.modifier.cap;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatCapModifierTest {

	@Test
	void retainsSourceMetadataAndAmount() {
		PlayerStatCapModifier modifier = new PlayerStatCapModifier(
				PlayerStatType.ATTACK_SPEED, "equipment:helmet", PlayerStatContributionSource.ARMOR,
				"Swift Helmet", 25
		);

		assertAll(
				() -> assertEquals(PlayerStatType.ATTACK_SPEED, modifier.statType()),
				() -> assertEquals("equipment:helmet", modifier.sourceId()),
				() -> assertEquals(PlayerStatContributionSource.ARMOR, modifier.source()),
				() -> assertEquals("Swift Helmet", modifier.displayName()),
				() -> assertEquals(25, modifier.amount())
		);
	}

	@Test
	void rejectsInvalidFields() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatCapModifier(null, "id", 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatCapModifier(PlayerStatType.ATTACK_SPEED, " ", 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatCapModifier(
						PlayerStatType.ATTACK_SPEED, "id", null, "Item", 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatCapModifier(
						PlayerStatType.ATTACK_SPEED, "id", PlayerStatContributionSource.ARMOR, " ", 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatCapModifier(
						PlayerStatType.ATTACK_SPEED, "id", Double.NaN))
		);
	}
}

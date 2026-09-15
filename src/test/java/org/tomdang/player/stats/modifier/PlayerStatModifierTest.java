package org.tomdang.player.stats.modifier;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatModifierTest {

	@Test
	void retainsItsStatSourceAndAmount() {
		PlayerStatModifier modifier = new PlayerStatModifier(
				PlayerStatType.DEFENSE,
				"armor:steel_chestplate",
				20
		);

		assertAll(
				() -> assertEquals(PlayerStatType.DEFENSE, modifier.getStatType()),
				() -> assertEquals("armor:steel_chestplate", modifier.getSourceId()),
				() -> assertEquals(20, modifier.getAmount(), 0.000001)
		);
	}

	@Test
	void negativeAmountIsAllowedForDebuffs() {
		PlayerStatModifier modifier = new PlayerStatModifier(
				PlayerStatType.STRENGTH,
				"debuff:weakness",
				-10
		);

		assertEquals(-10, modifier.getAmount(), 0.000001);
	}

	@Test
	void invalidDefinitionIsRejected() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatModifier(null, "source", 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatModifier(PlayerStatType.STRENGTH, null, 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatModifier(PlayerStatType.STRENGTH, "  ", 1)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatModifier(PlayerStatType.STRENGTH, "source", Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () -> new PlayerStatModifier(PlayerStatType.STRENGTH, "source", Double.POSITIVE_INFINITY))
		);
	}
}

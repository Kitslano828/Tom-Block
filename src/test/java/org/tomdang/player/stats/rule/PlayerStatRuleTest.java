package org.tomdang.player.stats.rule;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatRuleTest {

	@Test
	void acceptsUncappedAndValidCappedRules() {
		assertAll(
				() -> assertDoesNotThrow(() -> new PlayerStatRule(PlayerStatType.MAX_HEALTH, OptionalDouble.empty())),
				() -> assertDoesNotThrow(() -> new PlayerStatRule(PlayerStatType.MAX_HEALTH, OptionalDouble.of(250)))
		);
	}

	@Test
	void rejectsInvalidArguments() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatRule(null, OptionalDouble.empty())),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatRule(PlayerStatType.MAX_HEALTH, null)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatRule(PlayerStatType.MAX_HEALTH, OptionalDouble.of(Double.NaN))),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatRule(PlayerStatType.MAX_HEALTH, OptionalDouble.of(0)))
		);
	}
}

package org.tomdang.player.stats.evaluation;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatCalculationTest {

	@Test
	void reportsWhetherTheRawValueWasCapped() {
		assertAll(
				() -> assertTrue(new PlayerStatCalculation(
						PlayerStatType.DEFENSE, 300, 250, OptionalDouble.of(250)).capped()),
				() -> assertFalse(new PlayerStatCalculation(
						PlayerStatType.DEFENSE, 200, 200, OptionalDouble.of(250)).capped()),
				() -> assertFalse(new PlayerStatCalculation(
						PlayerStatType.DEFENSE, 300, 300, OptionalDouble.empty()).capped())
		);
	}

	@Test
	void rejectsInvalidValues() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatCalculation(null, 0, 0, OptionalDouble.empty())),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatCalculation(PlayerStatType.DEFENSE, Double.NaN, 0, OptionalDouble.empty())),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatCalculation(PlayerStatType.DEFENSE, 0, Double.NaN, OptionalDouble.empty())),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new PlayerStatCalculation(PlayerStatType.DEFENSE, 0, 0, null))
		);
	}
}

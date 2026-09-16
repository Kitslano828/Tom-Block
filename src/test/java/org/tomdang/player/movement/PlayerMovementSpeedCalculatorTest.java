package org.tomdang.player.movement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerMovementSpeedCalculatorTest {
	private final PlayerMovementSpeedCalculator calculator = new PlayerMovementSpeedCalculator(
			new PlayerMovementSpeedSettings(100, 0.2, 0, 1));

	@Test
	void convertsSpeedRelativeToConfiguredReference() {
		assertEquals(0, calculator.calculate(0), 0.000001);
		assertEquals(0.2, calculator.calculate(100), 0.000001);
		assertEquals(0.4, calculator.calculate(200), 0.000001);
		assertEquals(0.8, calculator.calculate(400), 0.000001);
	}

	@Test
	void clampsValuesToBukkitSafeRange() {
		assertEquals(0, calculator.calculate(-100), 0.000001);
		assertEquals(1, calculator.calculate(1000), 0.000001);
	}

	@Test
	void rejectsNonFiniteValuesAndInvalidDependency() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerMovementSpeedCalculator(null));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(Double.POSITIVE_INFINITY));
	}
}

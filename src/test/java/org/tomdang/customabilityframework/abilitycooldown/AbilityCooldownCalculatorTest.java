package org.tomdang.customabilityframework.abilitycooldown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbilityCooldownCalculatorTest {

	private final AbilityCooldownCalculator calculator = new AbilityCooldownCalculator();

	@Test
	void zeroHasteKeepsBaseCooldown() {
		assertEquals(40, calculator.calculate(40, 0));
	}

	@Test
	void appliesDiminishingReturnsAndRoundsUpToAWholeTick() {
		assertEquals(27, calculator.calculate(40, 50));
		assertEquals(20, calculator.calculate(40, 100));
		assertEquals(14, calculator.calculate(40, 200));
	}

	@Test
	void zeroCooldownRemainsZero() {
		assertEquals(0, calculator.calculate(0, 0));
		assertEquals(0, calculator.calculate(0, 10_000));
	}

	@Test
	void positiveCooldownNeverFallsBelowOneTick() {
		assertEquals(1, calculator.calculate(1, Double.MAX_VALUE));
		assertEquals(1, calculator.calculate(40, Double.MAX_VALUE));
	}

	@Test
	void invalidInputsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(40, -1));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(40, Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(40, Double.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(40, Double.NEGATIVE_INFINITY));
	}
}

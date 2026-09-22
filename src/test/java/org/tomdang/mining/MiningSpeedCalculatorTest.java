package org.tomdang.mining;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiningSpeedCalculatorTest {
	private final MiningSpeedCalculator calculator = new MiningSpeedCalculator();

	@Test
	void usesStrengthTimesThirtyDividedByEffectiveMiningSpeed() {
		assertEquals(20, calculator.ticksToBreak(10, 15));
		assertEquals(7, calculator.ticksToBreak(10, 45));
		assertEquals(14, calculator.ticksToBreak(20, 45));
		assertEquals(1, calculator.ticksToBreak(10, 2500));
	}

	@Test
	void zeroSpeedCannotProgressAndFractionalTicksRoundUp() {
		assertEquals(Long.MAX_VALUE, calculator.ticksToBreak(10, 0));
		assertEquals(1, calculator.ticksToBreak(1, 1000));
		assertEquals(2, calculator.ticksToBreak(1, 16));
	}

	@Test
	void rejectsInvalidInputs() {
		assertThrows(IllegalArgumentException.class, () -> calculator.ticksToBreak(0, 15));
		assertThrows(IllegalArgumentException.class, () -> calculator.ticksToBreak(10, -1));
		assertThrows(IllegalArgumentException.class, () -> calculator.ticksToBreak(10, Double.NaN));
	}
}

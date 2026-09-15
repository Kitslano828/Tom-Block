package org.tomdang.combat.combo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComboTimingCalculatorTest {
	private final ComboTimingCalculator calculator = new ComboTimingCalculator(
			new ComboTimingConfiguration(20, 6, 42));

	@Test
	void linearlyScalesFromFullGraceAtHitOneToMinimumAtHitFortyTwo() {
		assertAll(
				() -> assertEquals(new ComboTimingCalculation(20, 60), calculator.calculate(1, 40)),
				() -> assertEquals(new ComboTimingCalculation(13, 53), calculator.calculate(22, 40)),
				() -> assertEquals(new ComboTimingCalculation(6, 46), calculator.calculate(42, 40))
		);
	}

	@Test
	void graceStaysAtItsMinimumAfterHitFortyTwo() {
		assertEquals(new ComboTimingCalculation(6, 38), calculator.calculate(100, 32));
	}

	@Test
	void effectiveRecoveryIsAlwaysAddedToGrace() {
		assertAll(
				() -> assertEquals(20, calculator.calculate(1, 0).maximumGapTicks()),
				() -> assertEquals(60, calculator.calculate(1, 40).maximumGapTicks())
		);
	}

	@Test
	void rejectsInvalidInputs() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> new ComboTimingCalculator(null)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0, 20)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(1, -1))
		);
	}
}

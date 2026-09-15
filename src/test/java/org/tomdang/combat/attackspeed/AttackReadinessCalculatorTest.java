package org.tomdang.combat.attackspeed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackReadinessCalculatorTest {
	private final AttackReadinessCalculator calculator = new AttackReadinessCalculator();

	@Test
	void reportsEmptyPartialAndFullReadiness() {
		AttackReadinessCalculation empty = calculator.calculate(0, 20, 0);
		AttackReadinessCalculation partial = calculator.calculate(10, 20, 0);
		AttackReadinessCalculation full = calculator.calculate(20, 20, 0);

		assertEquals(0, empty.readiness());
		assertFalse(empty.fullyCharged());
		assertEquals(0.5, partial.readiness());
		assertFalse(partial.fullyCharged());
		assertEquals(1, full.readiness());
		assertTrue(full.fullyCharged());
	}

	@Test
	void attackSpeedChangesRecoveryBeforeReadinessIsCalculated() {
		AttackReadinessCalculation result = calculator.calculate(5, 20, 250);

		assertEquals(16, result.effectiveRecoveryTicks());
		assertEquals(5.0 / 16.0, result.readiness());
	}

	@Test
	void readinessNeverExceedsOneAndZeroAttackSpeedUsesNormalRecovery() {
		assertEquals(1, calculator.calculate(100, 20, 0).readiness());
		AttackReadinessCalculation baseline = calculator.calculate(10, 20, 0);
		assertEquals(0.5, baseline.readiness());
		assertFalse(baseline.fullyCharged());
	}

	@Test
	void rejectsInvalidInputsAndDependencies() {
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1, 20, 100));
		assertThrows(IllegalArgumentException.class, () -> new AttackReadinessCalculator(null));
	}
}

package org.tomdang.combat.attackspeed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttackIndicatorAttributeCalculatorTest {
	private final AttackIndicatorAttributeCalculator calculator = new AttackIndicatorAttributeCalculator();

	@Test
	void convertsEffectiveRecoveryTicksToVanillaAttackSpeed() {
		assertEquals(1.0, calculator.desiredValue(20), 0.000001);
		assertEquals(1.25, calculator.desiredValue(16), 0.000001);
		assertEquals(2.0, calculator.desiredValue(10), 0.000001);
		assertEquals(20.0, calculator.desiredValue(0), 0.000001);
	}

	@Test
	void compensatesForVanillaHeldItemModifiers() {
		// An iron sword commonly turns base 4.0 into final 1.6. Targeting 1.25 therefore needs 3.65.
		assertEquals(3.65, calculator.adjustedBase(4.0, 1.6, 1.25), 0.000001);
	}

	@Test
	void rejectsInvalidValues() {
		assertThrows(IllegalArgumentException.class, () -> calculator.desiredValue(-1));
		assertThrows(IllegalArgumentException.class,
				() -> calculator.adjustedBase(Double.NaN, 1, 1));
		assertThrows(IllegalArgumentException.class,
				() -> calculator.adjustedBase(1, Double.POSITIVE_INFINITY, 1));
	}
}

package org.tomdang.combat.damage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JellyfishHuntingDamageCalculatorTest {
	private final JellyfishHuntingDamageCalculator calculator = new JellyfishHuntingDamageCalculator();

	@Test
	void skillBonusIncreasesNetPowerByFiftyPercent() {
		assertEquals(15.0, calculator.calculate(10.0, 50.0));
	}

	@Test
	void upgradedNetPowerChangesBaseDamage() {
		assertEquals(30.0, calculator.calculate(20.0, 50.0));
	}

	@Test
	void rejectsInvalidInputs() {
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(Double.NaN, 0));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(10, Double.POSITIVE_INFINITY));
	}
}

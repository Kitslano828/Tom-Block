package org.tomdang.combat.damage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerDamageCalculatorTest {

	private final PlayerDamageCalculator calculator = new PlayerDamageCalculator();

	@Test
	void zeroDamageUsesUnarmedBaseDamage() {
		assertEquals(1, calculator.calculateBasicAttack(0, 0), 0.000001);
	}

	@Test
	void negativeDamageUsesUnarmedBaseDamage() {
		assertEquals(1, calculator.calculateBasicAttack(-5, 0), 0.000001);
	}

	@Test
	void damageDoesNotDependOnItemCategory() {
		assertEquals(500, calculator.calculateBasicAttack(500, 0), 0.000001);
	}

	@Test
	void strengthAppliesCurrentMultiplier() {
		assertEquals(600, calculator.calculateBasicAttack(500, 100), 0.000001);
	}

	@Test
	void negativeStrengthCannotReduceDamageBelowItsBase() {
		assertEquals(500, calculator.calculateBasicAttack(500, -100), 0.000001);
	}

	@Test
	void nonFiniteInputsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> calculator.calculateBasicAttack(Double.NaN, 0));
		assertThrows(IllegalArgumentException.class,
				() -> calculator.calculateBasicAttack(1, Double.POSITIVE_INFINITY));
	}
}

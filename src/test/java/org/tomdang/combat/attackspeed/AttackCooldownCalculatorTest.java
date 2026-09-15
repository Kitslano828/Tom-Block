package org.tomdang.combat.attackspeed;

import org.junit.jupiter.api.Test;

import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackCooldownCalculatorTest {

	private final AttackCooldownCalculator calculator = new AttackCooldownCalculator();

	@Test
	void oneHundredAttackSpeedKeepsTheBaseCooldown() {
		assertEquals(OptionalLong.of(20), calculator.calculate(20, 100));
	}

	@Test
	void attackSpeedScalesCooldownInBothDirections() {
		assertAll(
				() -> assertEquals(OptionalLong.of(40), calculator.calculate(20, 50)),
				() -> assertEquals(OptionalLong.of(10), calculator.calculate(20, 200)),
				() -> assertEquals(OptionalLong.of(8), calculator.calculate(20, 250))
		);
	}

	@Test
	void fractionalResultsRoundUpToAvoidAttackingTooEarly() {
		assertEquals(OptionalLong.of(14), calculator.calculate(20, 150));
	}

	@Test
	void positiveBaseCooldownAlwaysTakesAtLeastOneTick() {
		assertEquals(OptionalLong.of(1), calculator.calculate(1, Double.MAX_VALUE));
	}

	@Test
	void zeroAttackSpeedMeansAttacksAreUnavailable() {
		assertTrue(calculator.calculate(20, 0).isEmpty());
	}

	@Test
	void zeroBaseCooldownRemainsZeroWhenAttacksAreAvailable() {
		assertEquals(OptionalLong.of(0), calculator.calculate(0, 100));
	}

	@Test
	void rejectsInvalidInputsAndOverflow() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1, 100)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, -1)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(Long.MAX_VALUE, 0.1))
		);
	}
}

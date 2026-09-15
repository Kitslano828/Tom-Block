package org.tomdang.combat.attackspeed;

import org.junit.jupiter.api.Test;

import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttackRecoveryCalculatorTest {

	private final AttackRecoveryCalculator calculator = new AttackRecoveryCalculator();

	@Test
	void zeroAttackSpeedKeepsTheBaseRecovery() {
		assertEquals(OptionalLong.of(20), calculator.calculate(20, 0));
	}

	@Test
	void attackSpeedProvidesOnePercentRecoveryRatePerTenPoints() {
		assertAll(
				() -> assertEquals(OptionalLong.of(20), calculator.calculate(20, 0)),
				() -> assertEquals(OptionalLong.of(19), calculator.calculate(20, 100)),
				() -> assertEquals(OptionalLong.of(16), calculator.calculate(20, 250))
		);
	}

	@Test
	void fractionalResultsRoundUpToAvoidAttackingTooEarly() {
		assertEquals(OptionalLong.of(18), calculator.calculate(20, 150));
	}

	@Test
	void positiveBaseCooldownAlwaysTakesAtLeastOneTick() {
		assertEquals(OptionalLong.of(1), calculator.calculate(1, Double.MAX_VALUE));
	}

	@Test
	void zeroBaseCooldownRemainsZeroWhenAttacksAreAvailable() {
		assertEquals(OptionalLong.of(0), calculator.calculate(0, 0));
	}

	@Test
	void rejectsInvalidInputs() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1, 100)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, -1)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () -> calculator.calculate(20, Double.POSITIVE_INFINITY))
		);
	}
}

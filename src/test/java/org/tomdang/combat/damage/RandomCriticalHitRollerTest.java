package org.tomdang.combat.damage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomCriticalHitRollerTest {
	@Test void comparesPercentageAgainstInjectedRoll() {
		assertTrue(new RandomCriticalHitRoller(() -> 0.2499).isCritical(25));
		assertFalse(new RandomCriticalHitRoller(() -> 0.25).isCritical(25));
	}

	@Test void clampsGuaranteedAndImpossibleChancesWithoutRolling() {
		RandomCriticalHitRoller roller = new RandomCriticalHitRoller(() -> { throw new AssertionError("must not roll"); });
		assertFalse(roller.isCritical(0));
		assertTrue(roller.isCritical(100));
	}

	@Test void validatesInputsAndRandomSource() {
		assertThrows(IllegalArgumentException.class, () -> new RandomCriticalHitRoller(null));
		assertThrows(IllegalArgumentException.class, () -> new RandomCriticalHitRoller(() -> 0.5).isCritical(Double.NaN));
		assertThrows(IllegalStateException.class, () -> new RandomCriticalHitRoller(() -> 1).isCritical(50));
	}
}

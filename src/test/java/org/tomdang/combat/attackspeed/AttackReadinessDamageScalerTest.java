package org.tomdang.combat.attackspeed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttackReadinessDamageScalerTest {
	private final AttackReadinessDamageScaler scaler = new AttackReadinessDamageScaler();

	@Test
	void usesVanillaStyleDamageCurve() {
		assertEquals(20, scaler.scale(100, 0), 0.000001);
		assertEquals(40, scaler.scale(100, 0.5), 0.000001);
		assertEquals(100, scaler.scale(100, 1), 0.000001);
	}

	@Test
	void rejectsInvalidInputs() {
		assertThrows(IllegalArgumentException.class, () -> scaler.scale(-1, 1));
		assertThrows(IllegalArgumentException.class, () -> scaler.scale(1, -0.1));
		assertThrows(IllegalArgumentException.class, () -> scaler.scale(1, 1.1));
	}
}

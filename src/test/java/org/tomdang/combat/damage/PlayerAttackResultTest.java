package org.tomdang.combat.damage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerAttackResultTest {
	@Test void rejectsInvalidDamage() {
		assertThrows(IllegalArgumentException.class, () -> new PlayerAttackResult(-1, false));
		assertThrows(IllegalArgumentException.class, () -> new PlayerAttackResult(Double.NaN, true));
	}
}

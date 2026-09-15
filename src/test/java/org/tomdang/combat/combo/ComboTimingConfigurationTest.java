package org.tomdang.combat.combo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComboTimingConfigurationTest {
	@Test
	void rejectsInvalidRangesAndEndpoints() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> new ComboTimingConfiguration(-1, 0, 42)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new ComboTimingConfiguration(20, -1, 42)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new ComboTimingConfiguration(6, 20, 42)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> new ComboTimingConfiguration(20, 6, 1))
		);
	}
}

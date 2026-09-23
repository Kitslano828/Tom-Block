package org.tomdang.foraging.encounter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChoppingPowerTest {
	private final ChoppingPower choppingPower = new ChoppingPower();

	@Test
	void convertsPowerToConsecutiveNodesAfterToughness() {
		assertEquals(1, choppingPower.consecutiveNodes(0, 20));
		assertEquals(1, choppingPower.consecutiveNodes(20, 20));
		assertEquals(2, choppingPower.consecutiveNodes(55, 20));
		assertEquals(5, choppingPower.consecutiveNodes(100, 20));
	}

	@Test
	void rejectsInvalidInputs() {
		assertThrows(IllegalArgumentException.class, () -> choppingPower.consecutiveNodes(-1, 20));
		assertThrows(IllegalArgumentException.class, () -> choppingPower.consecutiveNodes(Double.NaN, 20));
		assertThrows(IllegalArgumentException.class, () -> choppingPower.consecutiveNodes(20, 0));
	}
}

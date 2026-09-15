package org.tomdang.player.stats.menu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CenteredStatSlotCalculatorTest {
	private final CenteredStatSlotCalculator calculator = new CenteredStatSlotCalculator();

	@Test void centersSmallGroups() {
		assertEquals(List.of(22), calculator.calculate(1));
		assertEquals(List.of(20, 21, 22, 23), calculator.calculate(4));
		assertEquals(List.of(), calculator.calculate(0));
	}

	@Test void flowsLargeGroupsOntoCenteredRows() {
		assertEquals(List.of(18, 19, 20, 21, 22, 23, 24, 25, 26, 31), calculator.calculate(10));
	}

	@Test void rejectsCountsOutsideCapacity() {
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(37));
	}
}

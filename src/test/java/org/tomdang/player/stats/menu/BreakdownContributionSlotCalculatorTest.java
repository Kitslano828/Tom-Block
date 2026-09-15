package org.tomdang.player.stats.menu;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BreakdownContributionSlotCalculatorTest {
	private final BreakdownContributionSlotCalculator calculator = new BreakdownContributionSlotCalculator();

	@Test void startsAtTenAndReservesBorderColumns() {
		assertEquals(List.of(10, 11, 12, 13, 14, 15, 16, 19), calculator.calculate(8));
	}

	@Test void validatesCapacity() {
		assertEquals(28, calculator.calculate(28).size());
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(-1));
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(29));
	}
}

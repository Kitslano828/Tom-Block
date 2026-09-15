package org.tomdang.player.stats.menu;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BorderedMenuSlotCalculatorTest {
	private final BorderedMenuSlotCalculator calculator = new BorderedMenuSlotCalculator();

	@Test void contentStartsAtTenAndSkipsBorderColumns() {
		assertEquals(List.of(10, 11, 12, 13, 14, 15, 16, 19), calculator.contentSlots(8));
	}

	@Test void borderCoversPerimeterOnly() {
		List<Integer> border = calculator.borderSlots();
		assertAll(
				() -> assertEquals(26, border.size()),
				() -> assertTrue(border.containsAll(List.of(0, 8, 9, 17, 45, 53))),
				() -> assertFalse(border.contains(10)),
				() -> assertFalse(border.contains(40))
		);
	}

	@Test void enforcesInnerCapacity() {
		assertEquals(28, calculator.contentSlots(28).size());
		assertThrows(IllegalArgumentException.class, () -> calculator.contentSlots(-1));
		assertThrows(IllegalArgumentException.class, () -> calculator.contentSlots(29));
	}
}

package org.tomdang.player.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatValueFormatterTest {

	@Test
	void wholeValuesDoNotDisplayAnUnnecessaryDecimal() {
		assertEquals("200", PlayerStatValueFormatter.format(200.0));
	}

	@Test
	void floatingPointNoiseIsRemoved() {
		assertEquals("114.5", PlayerStatValueFormatter.format(114.50000000000001));
	}

	@Test
	void valuesAreRoundedToAtMostTwoDecimalPlaces() {
		assertAll(
				() -> assertEquals("12.35", PlayerStatValueFormatter.format(12.345)),
				() -> assertEquals("12.3", PlayerStatValueFormatter.format(12.3)),
				() -> assertEquals("0", PlayerStatValueFormatter.format(-0.0))
		);
	}

	@Test
	void nonFiniteValuesAreRejected() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> PlayerStatValueFormatter.format(Double.NaN)),
				() -> assertThrows(IllegalArgumentException.class, () -> PlayerStatValueFormatter.format(Double.POSITIVE_INFINITY)),
				() -> assertThrows(IllegalArgumentException.class, () -> PlayerStatValueFormatter.format(Double.NEGATIVE_INFINITY))
		);
	}
}

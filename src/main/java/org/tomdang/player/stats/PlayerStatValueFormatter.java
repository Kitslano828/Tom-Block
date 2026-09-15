package org.tomdang.player.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PlayerStatValueFormatter {

	private static final int DISPLAY_DECIMAL_PLACES = 2;

	private PlayerStatValueFormatter() {
	}

	public static String format(double value) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("value must be finite");
		}

		return BigDecimal.valueOf(value)
				.setScale(DISPLAY_DECIMAL_PLACES, RoundingMode.HALF_UP)
				.stripTrailingZeros()
				.toPlainString();
	}
}

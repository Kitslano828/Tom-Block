package org.tomdang.combat.combo;

public record ComboTimingCalculation(long graceTicks, long maximumGapTicks) {
	public ComboTimingCalculation {
		if (graceTicks < 0) throw new IllegalArgumentException("graceTicks cannot be negative");
		if (maximumGapTicks < graceTicks) {
			throw new IllegalArgumentException("maximumGapTicks cannot be less than graceTicks");
		}
	}
}

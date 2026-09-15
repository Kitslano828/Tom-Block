package org.tomdang.combat.combo;

public record ComboTimingConfiguration(
		long baseGraceTicks,
		long minimumGraceTicks,
		int hitsToMinimumGrace
) {
	public ComboTimingConfiguration {
		if (baseGraceTicks < 0) throw new IllegalArgumentException("baseGraceTicks cannot be negative");
		if (minimumGraceTicks < 0) throw new IllegalArgumentException("minimumGraceTicks cannot be negative");
		if (minimumGraceTicks > baseGraceTicks) {
			throw new IllegalArgumentException("minimumGraceTicks cannot exceed baseGraceTicks");
		}
		if (hitsToMinimumGrace < 2) {
			throw new IllegalArgumentException("hitsToMinimumGrace must be at least 2");
		}
	}
}

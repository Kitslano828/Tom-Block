package org.tomdang.foraging.encounter;

public final class ChoppingPower {
	public int consecutiveNodes(double power, double toughness) {
		if (!Double.isFinite(power) || power < 0) throw new IllegalArgumentException("power must be finite and non-negative");
		if (!Double.isFinite(toughness) || toughness <= 0) throw new IllegalArgumentException("toughness must be finite and positive");
		return Math.max(1, (int) Math.floor(power / toughness));
	}
}

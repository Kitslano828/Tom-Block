package org.tomdang.mining;

/** Converts configured block strength and effective Mining Speed to server ticks. */
public final class MiningSpeedCalculator {
	public long ticksToBreak(int blockStrength, double miningSpeed) {
		if (blockStrength <= 0) throw new IllegalArgumentException("blockStrength must be positive");
		if (!Double.isFinite(miningSpeed) || miningSpeed < 0)
			throw new IllegalArgumentException("miningSpeed must be finite and non-negative");
		if (miningSpeed == 0) return Long.MAX_VALUE;
		double ticks = Math.ceil(blockStrength * 30.0 / miningSpeed);
		return ticks >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(1L, (long) ticks);
	}
}

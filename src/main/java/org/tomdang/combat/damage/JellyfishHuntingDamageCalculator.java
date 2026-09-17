package org.tomdang.combat.damage;

/** A net's capture power is independent of Strength and critical-hit stats. */
public final class JellyfishHuntingDamageCalculator {
	public double calculate(double power, double percentageBonus) {
		if (!Double.isFinite(power) || power < 0) {
			throw new IllegalArgumentException("power must be finite and non-negative");
		}
		if (!Double.isFinite(percentageBonus)) {
			throw new IllegalArgumentException("percentageBonus must be finite");
		}
		double result = power * Math.max(0, 1 + percentageBonus / 100.0);
		if (!Double.isFinite(result)) throw new IllegalStateException("calculated hunting damage must be finite");
		return result;
	}
}

package org.tomdang.combat.attackspeed;

public class AttackReadinessDamageScaler {
	public double scale(double fullDamage, double readiness) {
		if (!Double.isFinite(fullDamage) || fullDamage < 0) {
			throw new IllegalArgumentException("fullDamage must be finite and non-negative");
		}
		if (!Double.isFinite(readiness) || readiness < 0 || readiness > 1) {
			throw new IllegalArgumentException("readiness must be finite and between 0 and 1");
		}
		return fullDamage * (0.2 + 0.8 * readiness * readiness);
	}
}

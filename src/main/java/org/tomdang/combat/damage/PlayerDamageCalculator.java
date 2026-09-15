package org.tomdang.combat.damage;

public class PlayerDamageCalculator {

	private static final double UNARMED_BASE_DAMAGE = 1.0;
	private static final double STRENGTH_MULTIPLIER_PER_POINT = 0.002;

	public double calculateBasicAttack(double effectiveDamage, double effectiveStrength) {
		requireFinite(effectiveDamage, "effectiveDamage");
		requireFinite(effectiveStrength, "effectiveStrength");

		double baseDamage = effectiveDamage > 0 ? effectiveDamage : UNARMED_BASE_DAMAGE;
		double strengthMultiplier = 1.0
				+ Math.max(0, effectiveStrength) * STRENGTH_MULTIPLIER_PER_POINT;
		double finalDamage = baseDamage * strengthMultiplier;

		if (!Double.isFinite(finalDamage)) {
			throw new IllegalStateException("calculated damage must be finite");
		}
		return Math.max(0, finalDamage);
	}

	private void requireFinite(double value, String name) {
		if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
	}
}

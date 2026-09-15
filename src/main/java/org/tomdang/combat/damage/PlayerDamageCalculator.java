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

	public PlayerAttackResult calculateBasicAttack(double effectiveDamage, double effectiveStrength,
			double effectiveCriticalDamage, boolean critical) {
		requireFinite(effectiveCriticalDamage, "effectiveCriticalDamage");
		double normalDamage = calculateBasicAttack(effectiveDamage, effectiveStrength);
		double multiplier = critical ? 1.0 + Math.max(0, effectiveCriticalDamage) / 100.0 : 1.0;
		double damage = normalDamage * multiplier;
		if (!Double.isFinite(damage)) throw new IllegalStateException("calculated critical damage must be finite");
		return new PlayerAttackResult(damage, critical);
	}

	private void requireFinite(double value, String name) {
		if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
	}
}

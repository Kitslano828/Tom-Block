package org.tomdang.combat.attackspeed;

public record AttackReadinessCalculation(
		double readiness,
		boolean fullyCharged,
		long effectiveRecoveryTicks
) {
	public AttackReadinessCalculation {
		if (!Double.isFinite(readiness) || readiness < 0 || readiness > 1) {
			throw new IllegalArgumentException("readiness must be finite and between 0 and 1");
		}
		if (effectiveRecoveryTicks < 0) {
			throw new IllegalArgumentException("effectiveRecoveryTicks cannot be negative");
		}
	}
}

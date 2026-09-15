package org.tomdang.combat.attackspeed;

import java.util.OptionalLong;

public class AttackReadinessCalculator {
	private final AttackRecoveryCalculator recoveryCalculator;

	public AttackReadinessCalculator() {
		this(new AttackRecoveryCalculator());
	}

	public AttackReadinessCalculator(AttackRecoveryCalculator recoveryCalculator) {
		if (recoveryCalculator == null) throw new IllegalArgumentException("recoveryCalculator cannot be null");
		this.recoveryCalculator = recoveryCalculator;
	}

	public AttackReadinessCalculation calculate(long elapsedTicks, long baseRecoveryTicks, double attackSpeed) {
		if (elapsedTicks < 0) throw new IllegalArgumentException("elapsedTicks cannot be negative");
		OptionalLong effectiveRecovery = recoveryCalculator.calculate(baseRecoveryTicks, attackSpeed);
		if (effectiveRecovery.isEmpty()) return new AttackReadinessCalculation(0, false, 0);

		long recoveryTicks = effectiveRecovery.getAsLong();
		if (recoveryTicks == 0) return new AttackReadinessCalculation(1, true, 0);
		double readiness = Math.min(1.0, (double) elapsedTicks / recoveryTicks);
		return new AttackReadinessCalculation(readiness, readiness >= 1.0, recoveryTicks);
	}
}

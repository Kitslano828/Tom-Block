package org.tomdang.combat.attackspeed;

public class AttackIndicatorAttributeCalculator {
	private static final double TICKS_PER_SECOND = 20.0;

	public double desiredValue(long effectiveRecoveryTicks) {
		if (effectiveRecoveryTicks < 0) {
			throw new IllegalArgumentException("effectiveRecoveryTicks cannot be negative");
		}
		return effectiveRecoveryTicks == 0
				? TICKS_PER_SECOND
				: TICKS_PER_SECOND / effectiveRecoveryTicks;
	}

	public double adjustedBase(double currentBase, double currentValue, double desiredValue) {
		if (!Double.isFinite(currentBase) || !Double.isFinite(currentValue) || !Double.isFinite(desiredValue)) {
			throw new IllegalArgumentException("attribute values must be finite");
		}
		return currentBase + desiredValue - currentValue;
	}
}

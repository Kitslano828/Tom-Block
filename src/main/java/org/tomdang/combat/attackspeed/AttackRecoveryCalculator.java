package org.tomdang.combat.attackspeed;

import java.util.OptionalLong;

public class AttackRecoveryCalculator {
	public OptionalLong calculate(long baseRecoveryTicks, double attackSpeed) {
		if (baseRecoveryTicks < 0) throw new IllegalArgumentException("baseRecoveryTicks cannot be negative");
		if (!Double.isFinite(attackSpeed)) throw new IllegalArgumentException("attackSpeed must be finite");
		if (attackSpeed < 0) throw new IllegalArgumentException("attackSpeed cannot be negative");
		if (baseRecoveryTicks == 0) return OptionalLong.of(0);
		double calculatedTicks = Math.ceil(baseRecoveryTicks / (1.0 + attackSpeed / 1000.0));
		if (!Double.isFinite(calculatedTicks) || calculatedTicks > Long.MAX_VALUE) {
			throw new IllegalArgumentException("calculated recovery exceeds the supported tick range");
		}
		return OptionalLong.of(Math.max(1, (long) calculatedTicks));
	}
}

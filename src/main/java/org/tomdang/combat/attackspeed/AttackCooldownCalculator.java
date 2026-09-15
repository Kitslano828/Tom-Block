package org.tomdang.combat.attackspeed;

import java.util.OptionalLong;

public class AttackCooldownCalculator {

	public OptionalLong calculate(long baseCooldownTicks, double attackSpeed) {
		if (baseCooldownTicks < 0) {
			throw new IllegalArgumentException("baseCooldownTicks cannot be negative");
		}
		if (!Double.isFinite(attackSpeed)) {
			throw new IllegalArgumentException("attackSpeed must be finite");
		}
		if (attackSpeed < 0) {
			throw new IllegalArgumentException("attackSpeed cannot be negative");
		}
		if (attackSpeed == 0) return OptionalLong.empty();
		if (baseCooldownTicks == 0) return OptionalLong.of(0);

		double calculatedTicks = Math.ceil(baseCooldownTicks * 100.0 / attackSpeed);
		if (!Double.isFinite(calculatedTicks) || calculatedTicks > Long.MAX_VALUE) {
			throw new IllegalArgumentException("calculated cooldown exceeds the supported tick range");
		}

		return OptionalLong.of(Math.max(1, (long) calculatedTicks));
	}
}

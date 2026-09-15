package org.tomdang.combat.configuration;

public record CombatTimingConfiguration(long defaultBasicAttackRecoveryTicks) {
	public CombatTimingConfiguration {
		if (defaultBasicAttackRecoveryTicks < 0) {
			throw new IllegalArgumentException("defaultBasicAttackRecoveryTicks cannot be negative");
		}
	}
}

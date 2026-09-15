package org.tomdang.combat.configuration;

public record CombatTimingConfiguration(long defaultBasicAttackCooldownTicks) {
	public CombatTimingConfiguration {
		if (defaultBasicAttackCooldownTicks < 0) {
			throw new IllegalArgumentException("defaultBasicAttackCooldownTicks cannot be negative");
		}
	}
}

package org.tomdang.customitemframework.combat;

import java.util.Optional;
import java.util.OptionalLong;

public record CustomItemCombatProfile(
		Optional<CombatWeightClass> weightClass,
		Optional<CombatDamageType> damageType,
		OptionalLong baseRecoveryTicks
) {
	public CustomItemCombatProfile {
		if (weightClass == null || damageType == null || baseRecoveryTicks == null) {
			throw new IllegalArgumentException("combat profile optionals cannot be null");
		}
		if (weightClass.isPresent() != damageType.isPresent()) {
			throw new IllegalArgumentException("weight class and damage type must be configured together");
		}
		if (baseRecoveryTicks.isPresent() && baseRecoveryTicks.getAsLong() <= 0) {
			throw new IllegalArgumentException("baseRecoveryTicks must be positive when configured");
		}
	}

	public static CustomItemCombatProfile empty() {
		return new CustomItemCombatProfile(Optional.empty(), Optional.empty(), OptionalLong.empty());
	}
}

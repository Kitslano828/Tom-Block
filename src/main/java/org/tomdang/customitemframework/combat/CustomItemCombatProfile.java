package org.tomdang.customitemframework.combat;

import org.tomdang.combat.eligibility.AttackCapability;
import org.tomdang.combat.damage.BasicAttackCalculationProfile;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

public record CustomItemCombatProfile(
		Optional<CombatWeightClass> weightClass,
		Optional<CombatDamageType> damageType,
		OptionalLong baseRecoveryTicks,
		Set<AttackCapability> attackCapabilities,
		BasicAttackCalculationProfile basicAttackCalculationProfile
) {
	public CustomItemCombatProfile {
		if (weightClass == null || damageType == null || baseRecoveryTicks == null) throw new IllegalArgumentException("combat profile optionals cannot be null");

		if (weightClass.isPresent() != damageType.isPresent()) throw new IllegalArgumentException("weight class and damage type must be configured together");

		if (baseRecoveryTicks.isPresent() && baseRecoveryTicks.getAsLong() <= 0) throw new IllegalArgumentException("baseRecoveryTicks must be positive when configured");

		if (attackCapabilities == null) throw new IllegalArgumentException("attackCapabilities cannot be null");
		if (basicAttackCalculationProfile == null) throw new IllegalArgumentException("basicAttackCalculationProfile cannot be null");

		attackCapabilities = Set.copyOf(attackCapabilities);
	}

	public CustomItemCombatProfile(Optional<CombatWeightClass> weightClass, Optional<CombatDamageType> damageType,
	                               OptionalLong baseRecoveryTicks, Set<AttackCapability> attackCapabilities) {
		this(weightClass, damageType, baseRecoveryTicks, attackCapabilities, BasicAttackCalculationProfile.COMBAT);
	}

	// Overloaded 3-parameter constructor
	public CustomItemCombatProfile(Optional<CombatWeightClass> weightClass, Optional<CombatDamageType> damageType, OptionalLong baseRecoveryTicks) {
		this(weightClass, damageType, baseRecoveryTicks, Set.of());
	}

	public static CustomItemCombatProfile empty() {
		return new CustomItemCombatProfile(Optional.empty(), Optional.empty(), OptionalLong.empty(), Set.of());
	}
}

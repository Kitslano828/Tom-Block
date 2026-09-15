package org.tomdang.combat.weapons.configuration;

import org.bukkit.Material;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.customitemframework.combat.CustomItemCombatProfile;

import java.util.List;

public record WeaponDefinition(String id, Material material, String displayName, Rarity rarity,
		CustomItemStatModifiers statModifiers, CustomItemStatCapModifiers statCapModifiers,
		CustomItemCombatProfile combatProfile,
		List<String> abilityIDs) {
	public WeaponDefinition(String id, Material material, String displayName, Rarity rarity,
	                        CustomItemStatModifiers statModifiers, List<String> abilityIDs) {
		this(id, material, displayName, rarity, statModifiers, CustomItemStatCapModifiers.empty(),
				new CustomItemCombatProfile(java.util.Optional.of(CombatWeightClass.MEDIUM),
						java.util.Optional.of(CombatDamageType.SLASHING), java.util.OptionalLong.of(20)), abilityIDs);
	}

	public WeaponDefinition {
		if (combatProfile == null || combatProfile.weightClass().isEmpty()
				|| combatProfile.baseRecoveryTicks().isEmpty()) {
			throw new IllegalArgumentException("weapon definitions require a complete combat profile");
		}
	}

	public CombatWeightClass weaponClass() { return combatProfile.weightClass().orElseThrow(); }
	public CombatDamageType damageType() { return combatProfile.damageType().orElseThrow(); }
	public long baseRecoveryTicks() { return combatProfile.baseRecoveryTicks().orElseThrow(); }
	public double damage() {
		return statModifiers.get(PlayerStatType.DAMAGE);
	}

	public double strength() {
		return statModifiers.get(PlayerStatType.STRENGTH);
	}
}

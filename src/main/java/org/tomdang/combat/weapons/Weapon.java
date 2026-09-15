package org.tomdang.combat.weapons;

import org.bukkit.Material;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.customitemframework.combat.CustomItemCombatProfile;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Map;

public class Weapon extends CustomItem {
	// For Later:
	/*
		- Crit Chance
		- Double Hit Chance (for one hit to count as 2)
		- Attack Speed (reduce the cooldown before the player can attack again?)
	 */

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
				  double damage, double strength) {
		this(id, material, displayName, rarity, itemCategory, new CustomItemStatModifiers(Map.of(
				PlayerStatType.DAMAGE, damage,
				PlayerStatType.STRENGTH, strength
		)));
	}

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
				  CustomItemStatModifiers statModifiers) {
		this(id, material, displayName, rarity, itemCategory, statModifiers, CustomItemStatCapModifiers.empty());
	}

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
	              CustomItemStatModifiers statModifiers, CustomItemStatCapModifiers statCapModifiers) {
		this(id, material, displayName, rarity, itemCategory, statModifiers, statCapModifiers,
				CombatWeightClass.MEDIUM, CombatDamageType.SLASHING, 20);
	}

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
	              CustomItemStatModifiers statModifiers, CustomItemStatCapModifiers statCapModifiers,
	              CombatWeightClass weaponClass, CombatDamageType damageType, long baseRecoveryTicks) {
		this(id, material, displayName, rarity, itemCategory, statModifiers, statCapModifiers,
				new CustomItemCombatProfile(java.util.Optional.of(weaponClass), java.util.Optional.of(damageType),
						java.util.OptionalLong.of(baseRecoveryTicks)));
	}

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
	              CustomItemStatModifiers statModifiers, CustomItemStatCapModifiers statCapModifiers,
	              CustomItemCombatProfile combatProfile) {
		super(id, material, displayName, rarity, itemCategory, statModifiers, statCapModifiers, combatProfile);
		if (combatProfile.weightClass().isEmpty() || combatProfile.baseRecoveryTicks().isEmpty()) {
			throw new IllegalArgumentException("weapons require combat traits and base recovery ticks");
		}
	}

	public CombatWeightClass getWeaponClass() { return getCombatProfile().weightClass().orElseThrow(); }
	public CombatDamageType getDamageType() { return getCombatProfile().damageType().orElseThrow(); }
	public long getBaseRecoveryTicks() { return getCombatProfile().baseRecoveryTicks().orElseThrow(); }

	public double getDamage() {
		return getStatModifiers().get(PlayerStatType.DAMAGE);
	}

	public double getStrength() {
		return getStatModifiers().get(PlayerStatType.STRENGTH);
	}
}

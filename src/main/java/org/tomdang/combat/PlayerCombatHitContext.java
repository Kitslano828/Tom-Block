package org.tomdang.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.tomdang.combat.attackspeed.AttackReadinessCalculation;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.combat.CombatWeightClass;
import org.tomdang.customitemframework.combat.CombatDamageType;

import java.util.Optional;

public record PlayerCombatHitContext(
		Player attacker,
		LivingEntity target,
		Optional<CustomItem> item,
		Optional<CombatWeightClass> weightClass,
		Optional<CombatDamageType> damageType,
		AttackReadinessCalculation readiness,
		double fullDamage,
		double damage,
		boolean critical
) {
	public PlayerCombatHitContext {
		if (attacker == null) throw new IllegalArgumentException("attacker cannot be null");
		if (target == null) throw new IllegalArgumentException("target cannot be null");
		if (item == null || weightClass == null || damageType == null) {
			throw new IllegalArgumentException("item combat optionals cannot be null");
		}
		if (weightClass.isPresent() != damageType.isPresent()) {
			throw new IllegalArgumentException("combat traits must be present together");
		}
		if (readiness == null) throw new IllegalArgumentException("readiness cannot be null");
		if (!Double.isFinite(fullDamage) || fullDamage < 0) {
			throw new IllegalArgumentException("fullDamage must be finite and non-negative");
		}
		if (!Double.isFinite(damage) || damage < 0) {
			throw new IllegalArgumentException("damage must be finite and non-negative");
		}
	}

	public boolean fullyCharged() {
		return readiness.fullyCharged();
	}
}

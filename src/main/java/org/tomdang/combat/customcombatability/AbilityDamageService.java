package org.tomdang.combat.customcombatability;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.tomdang.combat.weapons.Weapon;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.combat.eligibility.AttackDelivery;
import org.tomdang.combat.eligibility.AttackSource;

import java.util.Set;


public class AbilityDamageService implements AbilityDamageHandler {

	private final CustomMobHealthService customMobHealthService;

	public AbilityDamageService(CustomMobHealthService customMobHealthService) {
		this.customMobHealthService = customMobHealthService;
	}

	@Override
	public void applyAbilityDamage(LivingEntity target, AbilityExecutionContext abilityExecutionContext, CustomCombatAbility ability) {

		Player player = abilityExecutionContext.getPlayer();
		double abilityDamage = ability.getAbilityDamage();
		CustomItem castingItem = abilityExecutionContext.getCastingItem();
		AttackSource source = new AttackSource(AttackDelivery.ABILITY,
				castingItem == null ? Set.of() : castingItem.getCombatProfile().attackCapabilities());

		if ( castingItem instanceof Weapon) {
			double baseDamage = ((Weapon) castingItem).getDamage();
			double totalDamage = baseDamage + abilityDamage;
			customMobHealthService.damageMob(player, target, totalDamage, source);

		} else {
			customMobHealthService.damageMob(player, target, abilityDamage, source);
		}

	}
}

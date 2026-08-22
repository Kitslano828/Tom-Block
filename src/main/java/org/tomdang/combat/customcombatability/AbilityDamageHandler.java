package org.tomdang.combat.customcombatability;

import org.bukkit.entity.LivingEntity;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;

public interface AbilityDamageHandler {

	void applyAbilityDamage(LivingEntity target, AbilityExecutionContext abilityExecutionContext, CustomCombatAbility ability);

}

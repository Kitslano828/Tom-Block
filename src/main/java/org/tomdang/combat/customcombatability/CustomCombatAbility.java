package org.tomdang.combat.customcombatability;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.customability.CustomAbility;

import java.util.ArrayList;
import java.util.List;


public abstract class CustomCombatAbility extends CustomAbility {

	@Getter
	private final double abilityDamage;

	public CustomCombatAbility(String abilityID, String abilityName, double energyCost, TomBlock instance, AbilityTrigger abilityTrigger, long cooldownInTicks, Component abilityDescription, double abilityDamage) {
		super(abilityID, abilityName, energyCost, instance, abilityTrigger, cooldownInTicks, abilityDescription);
		this.abilityDamage = abilityDamage;
	}

	@Override
	public List<Component> getAbilityStatLore() {
		List<Component> combatAbilityLore = new ArrayList<>();

		Component abilityDamageText = Component.text("Ability Damage: ", NamedTextColor.GRAY).
				append(Component.text((int)this.abilityDamage, NamedTextColor.RED)).decoration(TextDecoration.ITALIC,false);;

		combatAbilityLore.add(abilityDamageText);

		return combatAbilityLore;
	}
}

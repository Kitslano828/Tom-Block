package org.tomdang.mining.customminingability;

import net.kyori.adventure.text.Component;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.customability.CustomAbility;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomMiningAbility extends CustomAbility {

	public CustomMiningAbility(String abilityID, String abilityName, double energyCost, TomBlock instance, AbilityTrigger abilityTrigger, long cooldownInTicks, Component abilityDescription) {
		super(abilityID, abilityName, energyCost, instance, abilityTrigger, cooldownInTicks, abilityDescription);
	}

	@Override
	public List<Component> getAbilityStatLore() {
		// Nothing yet!
		List<Component> miningAbilityLore = new ArrayList<>();

		return miningAbilityLore;
	}
}

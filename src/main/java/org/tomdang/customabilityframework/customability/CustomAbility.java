package org.tomdang.customabilityframework.customability;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;

import java.util.ArrayList;
import java.util.List;


public abstract class CustomAbility {
	@Getter
	private final String abilityID;
	@Getter
	private final String abilityName;
	@Getter
	private final double energyCost;
	@Getter
	private final TomBlock instance;
	@Getter
	private final AbilityTrigger abilityTrigger;
	@Getter
	private final Component abilityDescription;
	@Getter
	private final long cooldownInTicks;

	public CustomAbility(String abilityID, String abilityName, double energyCost, TomBlock instance,
						 AbilityTrigger abilityTrigger, long cooldownInTicks,
						 Component abilityDescription) {
		this.abilityID = abilityID;
		this.abilityName = abilityName;
		this.energyCost = energyCost;
		this.instance = instance;
		this.abilityTrigger = abilityTrigger;
		this.abilityDescription = abilityDescription;
		this.cooldownInTicks = cooldownInTicks;
	}

	public List<Component> getAbilityStatLore() {
		return new ArrayList<>();
	};

	public abstract void execute(AbilityExecutionContext abilityExecutionContext);

}

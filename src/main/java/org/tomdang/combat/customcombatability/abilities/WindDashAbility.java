package org.tomdang.combat.customcombatability.abilities;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customabilityframework.customability.CustomAbility;

public class WindDashAbility extends CustomAbility {


	public WindDashAbility(String abilityID, String abilityName, double energyCost, TomBlock instance, AbilityTrigger abilityTrigger, long cooldownInTicks, Component abilityDescription) {
		super(abilityID, abilityName, energyCost, instance, abilityTrigger, cooldownInTicks, abilityDescription);
	}

	@Override
	public void execute(AbilityExecutionContext abilityExecutionContext) {
		Player player = abilityExecutionContext.getPlayer();

		Vector direction = player.getLocation().getDirection();

		direction.setY(0).normalize();

		direction.multiply(5);

		direction.setY(0.55);

		player.setVelocity(direction);
	}
}

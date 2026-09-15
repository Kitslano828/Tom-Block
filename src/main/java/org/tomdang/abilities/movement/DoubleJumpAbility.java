package org.tomdang.abilities.movement;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customabilityframework.customability.CustomAbility;

public class DoubleJumpAbility extends CustomAbility {

	private final double upwardVelocity;

	public DoubleJumpAbility(String abilityID, String abilityName, double energyCost, TomBlock instance,
	                         AbilityTrigger abilityTrigger, long cooldownInTicks,
	                         Component abilityDescription, double upwardVelocity) {
		super(abilityID, abilityName, energyCost, instance, abilityTrigger, cooldownInTicks, abilityDescription);
		if (!Double.isFinite(upwardVelocity) || upwardVelocity <= 0) {
			throw new IllegalArgumentException("upwardVelocity must be a positive finite number");
		}
		this.upwardVelocity = upwardVelocity;
	}

	@Override
	public boolean canActivate(AbilityExecutionContext abilityExecutionContext) {
		return !abilityExecutionContext.getPlayer().isOnGround();
	}

	@Override
	public void execute(AbilityExecutionContext abilityExecutionContext) {
		Player player = abilityExecutionContext.getPlayer();
		Vector velocity = player.getVelocity().clone();
		velocity.setY(upwardVelocity);
		player.setVelocity(velocity);
	}
}

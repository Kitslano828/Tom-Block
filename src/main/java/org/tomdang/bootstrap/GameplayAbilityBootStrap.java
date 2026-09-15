package org.tomdang.bootstrap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.TomBlock;
import org.tomdang.abilities.movement.DoubleJumpAbility;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityRegistry;

public class GameplayAbilityBootStrap {

	public GameplayAbilityBootStrap(TomBlock instance, CustomAbilityRegistry abilityRegistry) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (abilityRegistry == null) throw new IllegalArgumentException("abilityRegistry cannot be null");

		abilityRegistry.registerAbility(new DoubleJumpAbility(
				"DOUBLE_JUMP",
				"Double Jump",
				10,
				instance,
				AbilityTrigger.SNEAK,
				40,
				Component.text("Jump again while airborne", NamedTextColor.GRAY),
				0.75
		));
	}
}

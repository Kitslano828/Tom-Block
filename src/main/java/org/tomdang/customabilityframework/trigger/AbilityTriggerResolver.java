package org.tomdang.customabilityframework.trigger;

import org.bukkit.event.block.Action;
import org.tomdang.customabilityframework.AbilityTrigger;

import java.util.Optional;

public class AbilityTriggerResolver {

	public Optional<AbilityTrigger> resolveInteraction(Action action, boolean sneaking) {
		if (action == null) throw new IllegalArgumentException("action cannot be null");

		return switch (action) {
			case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> Optional.of(
					sneaking ? AbilityTrigger.SNEAK_RIGHT_CLICK : AbilityTrigger.RIGHT_CLICK
			);
			case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> Optional.of(
					sneaking ? AbilityTrigger.SNEAK_LEFT_CLICK : AbilityTrigger.LEFT_CLICK
			);
			default -> Optional.empty();
		};
	}

	public Optional<AbilityTrigger> resolveSneakChange(boolean sneaking) {
		return sneaking ? Optional.of(AbilityTrigger.SNEAK) : Optional.empty();
	}
}

package org.tomdang.customabilityframework.trigger;

import org.bukkit.event.block.Action;
import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.AbilityTrigger;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbilityTriggerResolverTest {

	private final AbilityTriggerResolver resolver = new AbilityTriggerResolver();

	@Test
	void resolvesNormalClickTriggers() {
		assertEquals(Optional.of(AbilityTrigger.RIGHT_CLICK),
				resolver.resolveInteraction(Action.RIGHT_CLICK_AIR, false));
		assertEquals(Optional.of(AbilityTrigger.RIGHT_CLICK),
				resolver.resolveInteraction(Action.RIGHT_CLICK_BLOCK, false));
		assertEquals(Optional.of(AbilityTrigger.LEFT_CLICK),
				resolver.resolveInteraction(Action.LEFT_CLICK_AIR, false));
		assertEquals(Optional.of(AbilityTrigger.LEFT_CLICK),
				resolver.resolveInteraction(Action.LEFT_CLICK_BLOCK, false));
	}

	@Test
	void resolvesSneakingClickTriggers() {
		assertEquals(Optional.of(AbilityTrigger.SNEAK_RIGHT_CLICK),
				resolver.resolveInteraction(Action.RIGHT_CLICK_AIR, true));
		assertEquals(Optional.of(AbilityTrigger.SNEAK_RIGHT_CLICK),
				resolver.resolveInteraction(Action.RIGHT_CLICK_BLOCK, true));
		assertEquals(Optional.of(AbilityTrigger.SNEAK_LEFT_CLICK),
				resolver.resolveInteraction(Action.LEFT_CLICK_AIR, true));
		assertEquals(Optional.of(AbilityTrigger.SNEAK_LEFT_CLICK),
				resolver.resolveInteraction(Action.LEFT_CLICK_BLOCK, true));
	}

	@Test
	void physicalAndSneakReleaseProduceNoTrigger() {
		assertEquals(Optional.empty(), resolver.resolveInteraction(Action.PHYSICAL, false));
		assertEquals(Optional.empty(), resolver.resolveSneakChange(false));
	}

	@Test
	void startingToSneakProducesSneakTrigger() {
		assertEquals(Optional.of(AbilityTrigger.SNEAK), resolver.resolveSneakChange(true));
	}

	@Test
	void nullActionIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> resolver.resolveInteraction(null, false));
	}
}

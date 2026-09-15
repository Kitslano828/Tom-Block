package org.tomdang.bootstrap;

import org.junit.jupiter.api.Test;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customabilityframework.customability.CustomAbility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GameplayAbilityBootStrapTest {

	@Test
	void registersDoubleJumpAbility() {
		CustomAbilityRegistry registry = new CustomAbilityRegistry();

		new GameplayAbilityBootStrap(mock(TomBlock.class), registry);

		CustomAbility ability = registry.getCustomAbility("DOUBLE_JUMP");
		assertNotNull(ability);
		assertEquals("Double Jump", ability.getAbilityName());
		assertEquals(AbilityTrigger.SNEAK, ability.getAbilityTrigger());
		assertEquals(10, ability.getEnergyCost(), 0.000001);
		assertEquals(40, ability.getCooldownInTicks());
	}

	@Test
	void nullDependenciesAreRejected() {
		TomBlock instance = mock(TomBlock.class);
		CustomAbilityRegistry registry = new CustomAbilityRegistry();

		assertThrows(IllegalArgumentException.class, () -> new GameplayAbilityBootStrap(null, registry));
		assertThrows(IllegalArgumentException.class, () -> new GameplayAbilityBootStrap(instance, null));
	}
}

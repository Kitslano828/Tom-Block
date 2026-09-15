package org.tomdang.customabilityframework.abilitylore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownCalculator;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.lore.ItemLoreContext;
import org.tomdang.player.stats.PlayerStatSnapshot;
import org.tomdang.player.stats.PlayerStatType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbilityLoreRendererTest {

	private final AbilityLoreRenderer renderer = new AbilityLoreRenderer();

	@Test
	void contextFreeRenderingUsesBaseCooldown() {
		assertEquals("Cooldown: 2s", cooldownLine(renderer.convertCustomAbilityToLore(ability(40))));
	}

	@Test
	void rendersCooldownUsingEffectiveAbilityHaste() {
		assertEquals("Cooldown: 2s", cooldownLine(renderer.convertCustomAbilityToLore(ability(40), context(0))));
		assertEquals("Cooldown: 1s", cooldownLine(renderer.convertCustomAbilityToLore(ability(40), context(100))));
		assertEquals("Cooldown: 0.7s", cooldownLine(renderer.convertCustomAbilityToLore(ability(40), context(200))));
	}

	@Test
	void rendersExactFractionalCooldownInsteadOfForcingHalfSeconds() {
		assertEquals("Cooldown: 1.35s", cooldownLine(renderer.convertCustomAbilityToLore(ability(40), context(50))));
	}

	@Test
	void invalidDependenciesAndInputsAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new AbilityLoreRenderer(null));
		assertThrows(IllegalArgumentException.class, () -> renderer.convertCustomAbilityToLore(null));
		assertThrows(IllegalArgumentException.class, () -> renderer.convertCustomAbilityToLore(ability(40), null));
	}

	private ItemLoreContext context(double abilityHaste) {
		return new ItemLoreContext(new PlayerStatSnapshot(Map.of(PlayerStatType.ABILITY_HASTE, abilityHaste)));
	}

	private CustomAbility ability(long cooldownTicks) {
		CustomAbility ability = mock(CustomAbility.class);
		when(ability.getAbilityName()).thenReturn("Test Ability");
		when(ability.getAbilityTrigger()).thenReturn(AbilityTrigger.RIGHT_CLICK);
		when(ability.getAbilityDescription()).thenReturn(Component.text("Description"));
		when(ability.getAbilityStatLore()).thenReturn(List.of());
		when(ability.getEnergyCost()).thenReturn(10.0);
		when(ability.getCooldownInTicks()).thenReturn(cooldownTicks);
		return ability;
	}

	private String cooldownLine(List<Component> lore) {
		return PlainTextComponentSerializer.plainText().serialize(lore.getLast());
	}
}

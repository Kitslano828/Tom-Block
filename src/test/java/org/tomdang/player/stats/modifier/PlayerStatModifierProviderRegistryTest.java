package org.tomdang.player.stats.modifier;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerStatModifierProviderRegistryTest {
	@Test
	void dynamicallyRegisteredProvidersContributeInRegistrationOrder() {
		Player player = mock(Player.class);
		PlayerStatModifier first = new PlayerStatModifier(PlayerStatType.ATTACK_SPEED, "first", 10);
		PlayerStatModifier second = new PlayerStatModifier(PlayerStatType.ATTACK_SPEED, "second", 20);
		PlayerStatModifierProviderRegistry registry = new PlayerStatModifierProviderRegistry();
		registry.register("equipment", provider(player, List.of(first)));

		assertEquals(List.of(first), registry.getModifiers(player));

		registry.register("region", provider(player, List.of(second)));
		assertEquals(List.of(first, second), registry.getModifiers(player));
		assertTrue(registry.isRegistered("region"));
	}

	@Test
	void unregisterRemovesProviderContributions() {
		Player player = mock(Player.class);
		PlayerStatModifierProviderRegistry registry = new PlayerStatModifierProviderRegistry();
		registry.register("region", provider(player,
				List.of(new PlayerStatModifier(PlayerStatType.ATTACK_SPEED, "region", 25))));

		assertTrue(registry.unregister("region"));
		assertFalse(registry.unregister("region"));
		assertTrue(registry.getModifiers(player).isEmpty());
	}

	@Test
	void rejectsDuplicateInvalidAndBrokenProviders() {
		Player player = mock(Player.class);
		PlayerStatModifierProviderRegistry registry = new PlayerStatModifierProviderRegistry();
		PlayerStatModifierProvider valid = provider(player, List.of());
		registry.register("valid", valid);

		assertThrows(IllegalStateException.class, () -> registry.register("valid", valid));
		assertThrows(IllegalArgumentException.class, () -> registry.register(" ", valid));
		assertThrows(IllegalArgumentException.class, () -> registry.register("null", null));
		assertThrows(IllegalArgumentException.class, () -> registry.getModifiers(null));

		PlayerStatModifierProvider nullCollection = mock(PlayerStatModifierProvider.class);
		when(nullCollection.getModifiers(player)).thenReturn(null);
		registry.register("null-collection", nullCollection);
		assertThrows(IllegalStateException.class, () -> registry.getModifiers(player));

		registry.unregister("null-collection");
		PlayerStatModifierProvider nullEntry = provider(player,
				Arrays.asList((PlayerStatModifier) null));
		registry.register("null-entry", nullEntry);
		assertThrows(IllegalStateException.class, () -> registry.getModifiers(player));
	}

	private PlayerStatModifierProvider provider(Player player, Collection<PlayerStatModifier> modifiers) {
		PlayerStatModifierProvider provider = mock(PlayerStatModifierProvider.class);
		when(provider.getModifiers(player)).thenReturn(modifiers);
		return provider;
	}
}

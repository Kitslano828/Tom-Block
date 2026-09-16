package org.tomdang.player.stats.modifier.cap;

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

class PlayerStatCapModifierProviderRegistryTest {
	@Test
	void dynamicallyRegisteredProvidersContributeInRegistrationOrder() {
		Player player = mock(Player.class);
		PlayerStatCapModifier first = new PlayerStatCapModifier(PlayerStatType.ATTACK_SPEED, "equipment", 25);
		PlayerStatCapModifier second = new PlayerStatCapModifier(PlayerStatType.ATTACK_SPEED, "region", -50);
		PlayerStatCapModifierProviderRegistry registry = new PlayerStatCapModifierProviderRegistry();
		registry.register("equipment", provider(player, List.of(first)));

		assertEquals(List.of(first), registry.getModifiers(player));

		registry.register("region", provider(player, List.of(second)));
		assertEquals(List.of(first, second), registry.getModifiers(player));
		assertTrue(registry.isRegistered("region"));
	}

	@Test
	void unregisterRemovesProviderContributions() {
		Player player = mock(Player.class);
		PlayerStatCapModifierProviderRegistry registry = new PlayerStatCapModifierProviderRegistry();
		registry.register("region", provider(player,
				List.of(new PlayerStatCapModifier(PlayerStatType.ATTACK_SPEED, "region", -50))));

		assertTrue(registry.unregister("region"));
		assertFalse(registry.unregister("region"));
		assertTrue(registry.getModifiers(player).isEmpty());
	}

	@Test
	void rejectsDuplicateInvalidAndBrokenProviders() {
		Player player = mock(Player.class);
		PlayerStatCapModifierProviderRegistry registry = new PlayerStatCapModifierProviderRegistry();
		PlayerStatCapModifierProvider valid = provider(player, List.of());
		registry.register("valid", valid);

		assertThrows(IllegalStateException.class, () -> registry.register("valid", valid));
		assertThrows(IllegalArgumentException.class, () -> registry.register(" ", valid));
		assertThrows(IllegalArgumentException.class, () -> registry.register("null", null));
		assertThrows(IllegalArgumentException.class, () -> registry.getModifiers(null));

		PlayerStatCapModifierProvider nullCollection = mock(PlayerStatCapModifierProvider.class);
		when(nullCollection.getModifiers(player)).thenReturn(null);
		registry.register("null-collection", nullCollection);
		assertThrows(IllegalStateException.class, () -> registry.getModifiers(player));

		registry.unregister("null-collection");
		PlayerStatCapModifierProvider nullEntry = provider(player,
				Arrays.asList((PlayerStatCapModifier) null));
		registry.register("null-entry", nullEntry);
		assertThrows(IllegalStateException.class, () -> registry.getModifiers(player));
	}

	private PlayerStatCapModifierProvider provider(Player player, Collection<PlayerStatCapModifier> modifiers) {
		PlayerStatCapModifierProvider provider = mock(PlayerStatCapModifierProvider.class);
		when(provider.getModifiers(player)).thenReturn(modifiers);
		return provider;
	}
}

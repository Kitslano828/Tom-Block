package org.tomdang.player.stats.modifier.cap;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompositePlayerStatCapModifierProviderTest {

	@Test
	void combinesProvidersInRegistrationOrder() {
		Player player = mock(Player.class);
		PlayerStatCapModifier first = modifier("first", 10);
		PlayerStatCapModifier second = modifier("second", 20);
		PlayerStatCapModifierProvider firstProvider = provider(player, List.of(first));
		PlayerStatCapModifierProvider secondProvider = provider(player, List.of(second));
		CompositePlayerStatCapModifierProvider composite =
				new CompositePlayerStatCapModifierProvider(List.of(firstProvider, secondProvider));

		Collection<PlayerStatCapModifier> result = composite.getModifiers(player);

		assertEquals(List.of(first, second), result);
		verify(firstProvider).getModifiers(player);
		verify(secondProvider).getModifiers(player);
		assertThrows(UnsupportedOperationException.class, result::clear);
	}

	@Test
	void validatesDependenciesAndProviderResults() {
		Player player = mock(Player.class);
		List<PlayerStatCapModifierProvider> withNullProvider = new ArrayList<>();
		withNullProvider.add(null);
		PlayerStatCapModifierProvider nullResult = mock(PlayerStatCapModifierProvider.class);
		when(nullResult.getModifiers(player)).thenReturn(null);
		PlayerStatCapModifierProvider nullEntry = mock(PlayerStatCapModifierProvider.class);
		List<PlayerStatCapModifier> withNullModifier = new ArrayList<>();
		withNullModifier.add(null);
		when(nullEntry.getModifiers(player)).thenReturn(withNullModifier);

		assertThrows(IllegalArgumentException.class, () -> new CompositePlayerStatCapModifierProvider(null));
		assertThrows(IllegalArgumentException.class, () -> new CompositePlayerStatCapModifierProvider(withNullProvider));
		assertThrows(IllegalArgumentException.class,
				() -> new CompositePlayerStatCapModifierProvider(List.of()).getModifiers(null));
		assertThrows(IllegalStateException.class,
				() -> new CompositePlayerStatCapModifierProvider(List.of(nullResult)).getModifiers(player));
		assertThrows(IllegalStateException.class,
				() -> new CompositePlayerStatCapModifierProvider(List.of(nullEntry)).getModifiers(player));
	}

	private PlayerStatCapModifierProvider provider(Player player, Collection<PlayerStatCapModifier> modifiers) {
		PlayerStatCapModifierProvider provider = mock(PlayerStatCapModifierProvider.class);
		when(provider.getModifiers(player)).thenReturn(modifiers);
		return provider;
	}

	private PlayerStatCapModifier modifier(String id, double amount) {
		return new PlayerStatCapModifier(PlayerStatType.ATTACK_SPEED, id, amount);
	}
}

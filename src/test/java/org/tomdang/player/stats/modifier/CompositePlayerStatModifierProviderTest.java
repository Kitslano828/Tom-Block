package org.tomdang.player.stats.modifier;

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

class CompositePlayerStatModifierProviderTest {

	@Test
	void emptyCompositeReturnsNoModifiers() {
		CompositePlayerStatModifierProvider composite = new CompositePlayerStatModifierProvider(List.of());

		assertEquals(List.of(), composite.getModifiers(mock(Player.class)));
	}

	@Test
	void combinesProvidersInRegistrationOrderAndCallsEachOnce() {
		Player player = mock(Player.class);
		PlayerStatModifier firstModifier = modifier("first", 10);
		PlayerStatModifier secondModifier = modifier("second", 20);
		PlayerStatModifierProvider first = provider(player, List.of(firstModifier));
		PlayerStatModifierProvider second = provider(player, List.of(secondModifier));
		CompositePlayerStatModifierProvider composite = new CompositePlayerStatModifierProvider(List.of(first, second));

		Collection<PlayerStatModifier> result = composite.getModifiers(player);

		assertEquals(List.of(firstModifier, secondModifier), result);
		verify(first).getModifiers(player);
		verify(second).getModifiers(player);
	}

	@Test
	void constructorCopiesProviderCollection() {
		Player player = mock(Player.class);
		List<PlayerStatModifierProvider> providers = new ArrayList<>();
		providers.add(provider(player, List.of(modifier("original", 10))));
		CompositePlayerStatModifierProvider composite = new CompositePlayerStatModifierProvider(providers);
		providers.clear();

		assertEquals(1, composite.getModifiers(player).size());
	}

	@Test
	void combinedResultIsImmutable() {
		Player player = mock(Player.class);
		PlayerStatModifierProvider provider = provider(player, List.of(modifier("armor", 10)));
		Collection<PlayerStatModifier> result = new CompositePlayerStatModifierProvider(List.of(provider)).getModifiers(player);

		assertThrows(UnsupportedOperationException.class, result::clear);
	}

	@Test
	void nullInputsAreRejected() {
		List<PlayerStatModifierProvider> providersWithNull = new ArrayList<>();
		providersWithNull.add(null);

		assertThrows(IllegalArgumentException.class, () -> new CompositePlayerStatModifierProvider(null));
		assertThrows(IllegalArgumentException.class, () -> new CompositePlayerStatModifierProvider(providersWithNull));
		assertThrows(
				IllegalArgumentException.class,
				() -> new CompositePlayerStatModifierProvider(List.of()).getModifiers(null)
		);
	}

	@Test
	void invalidProviderResultsAreRejected() {
		Player player = mock(Player.class);
		PlayerStatModifierProvider nullCollectionProvider = mock(PlayerStatModifierProvider.class);
		when(nullCollectionProvider.getModifiers(player)).thenReturn(null);
		PlayerStatModifierProvider nullEntryProvider = mock(PlayerStatModifierProvider.class);
		List<PlayerStatModifier> modifiersWithNull = new ArrayList<>();
		modifiersWithNull.add(null);
		when(nullEntryProvider.getModifiers(player)).thenReturn(modifiersWithNull);

		assertThrows(
				IllegalStateException.class,
				() -> new CompositePlayerStatModifierProvider(List.of(nullCollectionProvider)).getModifiers(player)
		);
		assertThrows(
				IllegalStateException.class,
				() -> new CompositePlayerStatModifierProvider(List.of(nullEntryProvider)).getModifiers(player)
		);
	}

	private PlayerStatModifierProvider provider(Player player, Collection<PlayerStatModifier> modifiers) {
		PlayerStatModifierProvider provider = mock(PlayerStatModifierProvider.class);
		when(provider.getModifiers(player)).thenReturn(modifiers);
		return provider;
	}

	private PlayerStatModifier modifier(String sourceId, double amount) {
		return new PlayerStatModifier(PlayerStatType.DEFENSE, sourceId, amount);
	}
}

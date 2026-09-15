package org.tomdang.customabilityframework.source;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompositeAbilitySourceProviderTest {

	@Test
	void emptyCompositeReturnsNoSources() {
		CompositeAbilitySourceProvider composite = new CompositeAbilitySourceProvider(List.of());

		assertEquals(List.of(), composite.getAbilitySources(mock(Player.class)));
	}

	@Test
	void combinesProvidersInRegistrationOrderAndCallsEachOnce() {
		Player player = mock(Player.class);
		AbilitySource firstSource = mock(AbilitySource.class);
		AbilitySource secondSource = mock(AbilitySource.class);
		AbilitySourceProvider first = provider(player, List.of(firstSource));
		AbilitySourceProvider second = provider(player, List.of(secondSource));
		CompositeAbilitySourceProvider composite = new CompositeAbilitySourceProvider(List.of(first, second));

		Collection<AbilitySource> result = composite.getAbilitySources(player);

		assertEquals(List.of(firstSource, secondSource), result);
		verify(first).getAbilitySources(player);
		verify(second).getAbilitySources(player);
	}

	@Test
	void constructorCopiesProviderCollection() {
		Player player = mock(Player.class);
		List<AbilitySourceProvider> providers = new ArrayList<>();
		providers.add(provider(player, List.of(mock(AbilitySource.class))));
		CompositeAbilitySourceProvider composite = new CompositeAbilitySourceProvider(providers);
		providers.clear();

		assertEquals(1, composite.getAbilitySources(player).size());
	}

	@Test
	void combinedResultIsImmutable() {
		Player player = mock(Player.class);
		AbilitySourceProvider provider = provider(player, List.of(mock(AbilitySource.class)));
		Collection<AbilitySource> result =
				new CompositeAbilitySourceProvider(List.of(provider)).getAbilitySources(player);

		assertThrows(UnsupportedOperationException.class, result::clear);
	}

	@Test
	void nullInputsAreRejected() {
		List<AbilitySourceProvider> providersWithNull = new ArrayList<>();
		providersWithNull.add(null);

		assertThrows(IllegalArgumentException.class, () -> new CompositeAbilitySourceProvider(null));
		assertThrows(IllegalArgumentException.class, () -> new CompositeAbilitySourceProvider(providersWithNull));
		assertThrows(IllegalArgumentException.class,
				() -> new CompositeAbilitySourceProvider(List.of()).getAbilitySources(null));
	}

	@Test
	void invalidProviderResultsAreRejected() {
		Player player = mock(Player.class);
		AbilitySourceProvider nullCollectionProvider = mock(AbilitySourceProvider.class);
		when(nullCollectionProvider.getAbilitySources(player)).thenReturn(null);
		AbilitySourceProvider nullEntryProvider = mock(AbilitySourceProvider.class);
		List<AbilitySource> sourcesWithNull = new ArrayList<>();
		sourcesWithNull.add(null);
		when(nullEntryProvider.getAbilitySources(player)).thenReturn(sourcesWithNull);

		assertThrows(IllegalStateException.class,
				() -> new CompositeAbilitySourceProvider(List.of(nullCollectionProvider)).getAbilitySources(player));
		assertThrows(IllegalStateException.class,
				() -> new CompositeAbilitySourceProvider(List.of(nullEntryProvider)).getAbilitySources(player));
	}

	private AbilitySourceProvider provider(Player player, Collection<AbilitySource> sources) {
		AbilitySourceProvider provider = mock(AbilitySourceProvider.class);
		when(provider.getAbilitySources(player)).thenReturn(sources);
		return provider;
	}
}

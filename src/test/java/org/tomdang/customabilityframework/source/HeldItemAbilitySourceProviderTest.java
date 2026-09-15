package org.tomdang.customabilityframework.source;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeldItemAbilitySourceProviderTest {

	@Test
	void nullDependencyAndPlayerAreRejected() {
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		HeldItemAbilitySourceProvider provider = new HeldItemAbilitySourceProvider(resolver);

		assertThrows(IllegalArgumentException.class, () -> new HeldItemAbilitySourceProvider(null));
		assertThrows(IllegalArgumentException.class, () -> provider.getAbilitySources(null));
	}

	@Test
	void unresolvedHeldItemProducesNoSources() {
		Fixture fixture = fixture(null);

		assertEquals(0, fixture.provider().getAbilitySources(fixture.player()).size());
	}

	@Test
	void customItemWithoutAbilitiesProducesNoSources() {
		Fixture fixture = fixture(item("PLAIN_ITEM"));

		assertEquals(0, fixture.provider().getAbilitySources(fixture.player()).size());
	}

	@Test
	void everyHeldItemAbilityBecomesAMainHandSource() {
		CustomItem item = item("WIND_BLADE");
		CustomAbility dash = ability("WIND_DASH_ABILITY");
		CustomAbility slash = ability("WIND_SLASH_ABILITY");
		item.addAbility(dash);
		item.addAbility(slash);
		Fixture fixture = fixture(item);

		Collection<AbilitySource> sources = fixture.provider().getAbilitySources(fixture.player());

		assertEquals(2, sources.size());
		assertSource(sources, dash, item,
				"equipment:main-hand:WIND_BLADE:WIND_DASH_ABILITY");
		assertSource(sources, slash, item,
				"equipment:main-hand:WIND_BLADE:WIND_SLASH_ABILITY");
	}

	@Test
	void returnedCollectionIsImmutable() {
		CustomItem item = item("WAND");
		item.addAbility(ability("MAGIC_BOLT_ABILITY"));
		Fixture fixture = fixture(item);
		Collection<AbilitySource> sources = fixture.provider().getAbilitySources(fixture.player());

		assertThrows(UnsupportedOperationException.class, sources::clear);
	}

	private Fixture fixture(CustomItem resolvedItem) {
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack heldItem = mock(ItemStack.class);
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(heldItem);
		when(resolver.getCustomItem(heldItem)).thenReturn(resolvedItem);
		return new Fixture(player, new HeldItemAbilitySourceProvider(resolver));
	}

	private CustomItem item(String id) {
		return new CustomItem(id, Material.STICK, id, Rarity.COMMON, ItemCategory.TEST_ITEM);
	}

	private CustomAbility ability(String id) {
		CustomAbility ability = mock(CustomAbility.class);
		when(ability.getAbilityID()).thenReturn(id);
		return ability;
	}

	private void assertSource(Collection<AbilitySource> sources, CustomAbility ability,
	                          CustomItem item, String sourceId) {
		AbilitySource source = sources.stream()
				.filter(candidate -> candidate.sourceId().equals(sourceId))
				.findFirst().orElseThrow();
		assertSame(ability, source.ability());
		assertSame(item, source.sourceItem());
		assertEquals(AbilitySourceType.MAIN_HAND, source.sourceType());
	}

	private record Fixture(Player player, HeldItemAbilitySourceProvider provider) {
	}
}

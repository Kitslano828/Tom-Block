package org.tomdang.customitemframework.stats;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.modifier.PlayerStatModifier;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeldItemStatModifierProviderTest {

	@Test
	void arbitraryCustomItemStatsBecomeHeldItemModifiers() {
		Fixture fixture = fixture(new CustomItem(
				"ODD_PICKAXE",
				Material.WOODEN_PICKAXE,
				"Odd Pickaxe",
				Rarity.COMMON,
				ItemCategory.MINING_TOOL,
				new CustomItemStatModifiers(Map.of(
						PlayerStatType.DAMAGE, 8.0,
						PlayerStatType.STRENGTH, 3.0,
						PlayerStatType.MINING_FORTUNE, 5.0
				))
		));

		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertEquals(3, modifiers.size());
		assertModifier(modifiers, PlayerStatType.DAMAGE, 8, "equipment:main-hand:ODD_PICKAXE:damage");
		assertModifier(modifiers, PlayerStatType.STRENGTH, 3, "equipment:main-hand:ODD_PICKAXE:strength");
		assertModifier(modifiers, PlayerStatType.MINING_FORTUNE, 5, "equipment:main-hand:ODD_PICKAXE:mining-fortune");
	}

	@Test
	void zeroContributionsAreOmittedAndResultIsImmutable() {
		Fixture fixture = fixture(new CustomItem(
				"PLAIN_ITEM",
				Material.STICK,
				"Plain Item",
				Rarity.COMMON,
				ItemCategory.TEST_ITEM,
				new CustomItemStatModifiers(Map.of(PlayerStatType.STRENGTH, 0.0))
		));

		Collection<PlayerStatModifier> modifiers = fixture.provider().getModifiers(fixture.player());

		assertEquals(0, modifiers.size());
		assertThrows(UnsupportedOperationException.class, modifiers::clear);
	}

	@Test
	void unresolvedHeldItemProducesNoModifiers() {
		Fixture fixture = fixture(null);

		assertEquals(0, fixture.provider().getModifiers(fixture.player()).size());
	}

	@Test
	void armorHeldInMainHandDoesNotGrantEquippedArmorStats() {
		Fixture fixture = fixture(new CustomItem(
				"RABBIT_BOOTS",
				Material.LEATHER_BOOTS,
				"Rabbit Boots",
				Rarity.COMMON,
				ItemCategory.ARMOR,
				new CustomItemStatModifiers(Map.of(PlayerStatType.ABILITY_HASTE, 100.0))
		));

		assertEquals(0, fixture.provider().getModifiers(fixture.player()).size());
	}

	@Test
	void nullDependencyAndPlayerAreRejected() {
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		HeldItemStatModifierProvider provider = new HeldItemStatModifierProvider(resolver);

		assertThrows(IllegalArgumentException.class, () -> new HeldItemStatModifierProvider(null));
		assertThrows(IllegalArgumentException.class, () -> provider.getModifiers(null));
	}

	private Fixture fixture(CustomItem customItem) {
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack heldItem = mock(ItemStack.class);
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(heldItem);
		when(resolver.getCustomItem(heldItem)).thenReturn(customItem);
		return new Fixture(player, new HeldItemStatModifierProvider(resolver));
	}

	private void assertModifier(
			Collection<PlayerStatModifier> modifiers,
			PlayerStatType statType,
			double amount,
			String sourceId
	) {
		PlayerStatModifier modifier = modifiers.stream()
				.filter(candidate -> candidate.getStatType() == statType)
				.findFirst()
				.orElseThrow();
		assertEquals(amount, modifier.getAmount(), 0.000001);
		assertEquals(sourceId, modifier.getSourceId());
	}

	private record Fixture(Player player, HeldItemStatModifierProvider provider) {
	}
}

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
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifier;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeldItemStatCapModifierProviderTest {

	@Test
	void heldNonArmorItemContributesItsConfiguredCapBonuses() {
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack stack = mock(ItemStack.class);
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		CustomItem item = new CustomItem("RACING_HELMET_TEST", Material.STICK, "Racing Item",
				Rarity.LEGENDARY, ItemCategory.WEAPON, CustomItemStatModifiers.empty(),
				new CustomItemStatCapModifiers(Map.of(PlayerStatType.ATTACK_SPEED, 100.0)));
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(stack);
		when(resolver.getCustomItem(stack)).thenReturn(item);

		Collection<PlayerStatCapModifier> modifiers =
				new HeldItemStatCapModifierProvider(resolver).getModifiers(player);

		assertEquals(1, modifiers.size());
		PlayerStatCapModifier modifier = modifiers.iterator().next();
		assertEquals(PlayerStatType.ATTACK_SPEED, modifier.statType());
		assertEquals(100, modifier.amount());
		assertEquals(PlayerStatContributionSource.HELD_ITEM, modifier.source());
		assertEquals("Racing Item", modifier.displayName());
	}

	@Test
	void missingItemProducesNoModifiersAndNullInputsAreRejected() {
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack stack = mock(ItemStack.class);
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(stack);
		when(resolver.getCustomItem(stack)).thenReturn(null);
		HeldItemStatCapModifierProvider provider = new HeldItemStatCapModifierProvider(resolver);

		assertEquals(0, provider.getModifiers(player).size());
		assertThrows(IllegalArgumentException.class, () -> provider.getModifiers(null));
		assertThrows(IllegalArgumentException.class, () -> new HeldItemStatCapModifierProvider(null));
	}
}

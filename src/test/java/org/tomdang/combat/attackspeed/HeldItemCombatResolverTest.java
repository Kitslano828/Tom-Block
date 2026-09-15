package org.tomdang.combat.attackspeed;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.configuration.CombatTimingConfiguration;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.combat.CustomItemCombatProfile;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;

import java.util.Optional;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeldItemCombatResolverTest {
	@Test
	void configuredItemRecoveryOverridesTheGlobalFallback() {
		HeldItemCombatResolver resolver = new HeldItemCombatResolver(
				mock(CustomItemResolver.class), new CombatTimingConfiguration(10));
		CustomItem item = new CustomItem("NET", Material.STICK, "Jellyfish Net", Rarity.COMMON,
				ItemCategory.TEST_ITEM, CustomItemStatModifiers.empty(), CustomItemStatCapModifiers.empty(),
				new CustomItemCombatProfile(Optional.empty(), Optional.empty(), OptionalLong.of(24)));

		assertEquals(24, resolver.resolveBaseRecoveryTicks(item));
		assertEquals(10, resolver.resolveBaseRecoveryTicks(null));
	}

	@Test
	void resolvesTheHeldCustomItemOnceThroughTheSharedResolver() {
		CustomItemResolver itemResolver = mock(CustomItemResolver.class);
		Player player = mock(Player.class);
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack stack = mock(ItemStack.class);
		CustomItem item = mock(CustomItem.class);
		when(player.getInventory()).thenReturn(inventory);
		when(inventory.getItemInMainHand()).thenReturn(stack);
		when(itemResolver.getCustomItem(stack)).thenReturn(item);

		assertEquals(item, new HeldItemCombatResolver(itemResolver,
				new CombatTimingConfiguration(10)).resolve(player));
	}

	@Test
	void rejectsNullDependenciesAndPlayers() {
		assertThrows(IllegalArgumentException.class,
				() -> new HeldItemCombatResolver(null, new CombatTimingConfiguration(10)));
		assertThrows(IllegalArgumentException.class,
				() -> new HeldItemCombatResolver(mock(CustomItemResolver.class), null));
		HeldItemCombatResolver resolver = new HeldItemCombatResolver(
				mock(CustomItemResolver.class), new CombatTimingConfiguration(10));
		assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null));
	}
}

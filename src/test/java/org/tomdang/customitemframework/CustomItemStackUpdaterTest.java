package org.tomdang.customitemframework;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.lore.ItemLoreContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CustomItemStackUpdaterTest {

	@Test
	void updatesOnlyDefinitionOwnedPresentation() {
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		CustomItemStackFactory factory = mock(CustomItemStackFactory.class);
		CustomItemStackUpdater updater = new CustomItemStackUpdater(resolver, factory);
		CustomItem definition = mock(CustomItem.class);
		ItemStack existingStack = mock(ItemStack.class);
		ItemStack templateStack = mock(ItemStack.class);
		ItemMeta existingMeta = mock(ItemMeta.class);
		ItemMeta templateMeta = mock(ItemMeta.class);
		ItemLoreContext context = ItemLoreContext.defaults();
		Component displayName = Component.text("Rabbit Boots");
		List<Component> lore = List.of(Component.text("Cooldown: 1s"));

		when(resolver.getCustomItem(existingStack)).thenReturn(definition);
		when(factory.createCustomItemStack(definition, context)).thenReturn(templateStack);
		when(existingStack.getItemMeta()).thenReturn(existingMeta);
		when(templateStack.getItemMeta()).thenReturn(templateMeta);
		when(templateMeta.displayName()).thenReturn(displayName);
		when(templateMeta.lore()).thenReturn(lore);

		assertTrue(updater.update(existingStack, context));

		verify(existingMeta).displayName(displayName);
		verify(existingMeta).lore(lore);
		verify(existingStack).setItemMeta(existingMeta);
		verify(existingStack, never()).setAmount(org.mockito.ArgumentMatchers.anyInt());
	}

	@Test
	void ignoresItemsWithoutRegisteredCustomItemDefinitions() {
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		CustomItemStackFactory factory = mock(CustomItemStackFactory.class);
		CustomItemStackUpdater updater = new CustomItemStackUpdater(resolver, factory);
		ItemStack itemStack = mock(ItemStack.class);

		when(resolver.getCustomItem(itemStack)).thenReturn(null);

		assertFalse(updater.update(itemStack, ItemLoreContext.defaults()));
		verifyNoInteractions(factory);
		verify(itemStack, never()).setItemMeta(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void missingMetadataOnResolvedCustomItemIsRejected() {
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		CustomItemStackFactory factory = mock(CustomItemStackFactory.class);
		CustomItemStackUpdater updater = new CustomItemStackUpdater(resolver, factory);
		CustomItem definition = mock(CustomItem.class);
		ItemStack existingStack = mock(ItemStack.class);
		ItemStack templateStack = mock(ItemStack.class);
		ItemLoreContext context = ItemLoreContext.defaults();

		when(resolver.getCustomItem(existingStack)).thenReturn(definition);
		when(factory.createCustomItemStack(definition, context)).thenReturn(templateStack);
		when(existingStack.getItemMeta()).thenReturn(null);

		assertThrows(IllegalStateException.class, () -> updater.update(existingStack, context));
	}

	@Test
	void invalidDependenciesAndInputsAreRejected() {
		CustomItemResolver resolver = mock(CustomItemResolver.class);
		CustomItemStackFactory factory = mock(CustomItemStackFactory.class);

		assertThrows(IllegalArgumentException.class, () -> new CustomItemStackUpdater(null, factory));
		assertThrows(IllegalArgumentException.class, () -> new CustomItemStackUpdater(resolver, null));

		CustomItemStackUpdater updater = new CustomItemStackUpdater(resolver, factory);
		assertThrows(IllegalArgumentException.class, () -> updater.update(null, ItemLoreContext.defaults()));
		assertThrows(IllegalArgumentException.class, () -> updater.update(mock(ItemStack.class), null));
	}
}

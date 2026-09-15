package org.tomdang.customitemframework;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tomdang.customitemframework.lore.ItemLoreContext;

public final class CustomItemStackUpdater {

	private final CustomItemResolver customItemResolver;
	private final CustomItemStackFactory customItemStackFactory;

	public CustomItemStackUpdater(CustomItemResolver customItemResolver,
	                              CustomItemStackFactory customItemStackFactory) {
		if (customItemResolver == null) throw new IllegalArgumentException("customItemResolver cannot be null");
		if (customItemStackFactory == null) throw new IllegalArgumentException("customItemStackFactory cannot be null");
		this.customItemResolver = customItemResolver;
		this.customItemStackFactory = customItemStackFactory;
	}

	public boolean update(ItemStack itemStack, ItemLoreContext context) {
		if (itemStack == null) throw new IllegalArgumentException("itemStack cannot be null");
		if (context == null) throw new IllegalArgumentException("context cannot be null");

		CustomItem customItem = customItemResolver.getCustomItem(itemStack);
		if (customItem == null) return false;

		ItemStack presentationTemplate = customItemStackFactory.createCustomItemStack(customItem, context);
		ItemMeta existingMeta = itemStack.getItemMeta();
		ItemMeta templateMeta = presentationTemplate.getItemMeta();
		if (existingMeta == null || templateMeta == null) {
			throw new IllegalStateException("Custom item presentation requires item metadata");
		}

		existingMeta.displayName(templateMeta.displayName());
		existingMeta.lore(templateMeta.lore());
		itemStack.setItemMeta(existingMeta);
		return true;
	}
}

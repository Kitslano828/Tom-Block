package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerStatMenuItemFactory {
	public ItemStack create(PlayerStatMenuItemDefinition definition) {
		if (definition == null) throw new IllegalArgumentException("definition cannot be null");
		ItemStack item = new ItemStack(definition.material());
		ItemMeta meta = item.getItemMeta();
		meta.itemName(definition.displayName());
		meta.lore(definition.lore());
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		item.setItemMeta(meta);
		return item;
	}

	public ItemStack createFiller(Material material) {
		if (material == null) throw new IllegalArgumentException("material cannot be null");
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.empty());
		meta.setHideTooltip(true);
		item.setItemMeta(meta);
		return item;
	}
}

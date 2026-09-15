package org.tomdang.customitemframework;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.customitemframework.stats.CustomItemStatLoreRenderer;

import java.util.List;

public class CustomItemCreator {
	private final NamespacedKey customItemIdKey;
	private final CustomItemStatLoreRenderer statLoreRenderer = new CustomItemStatLoreRenderer();

	public CustomItemCreator(NamespacedKey customItemIdKey) {
		this.customItemIdKey = customItemIdKey;
	}

	public ItemStack createItemStack(CustomItem customItem) {
		ItemStack item;

		item = ItemStack.of(customItem.getMaterial());
		ItemMeta meta = item.getItemMeta();

		meta.getPersistentDataContainer().set(customItemIdKey, PersistentDataType.STRING, customItem.getId());
		meta.displayName(Component.text(customItem.getDisplayName()).color(customItem.getRarity().getColor()).decoration(TextDecoration.ITALIC, false));
		List<Component> statLore = statLoreRenderer.render(customItem.getStatModifiers());
		if (!statLore.isEmpty()) meta.lore(statLore);

		item.setItemMeta(meta);
		return item;
	}

	protected List<Component> renderStatLore(CustomItem customItem) {
		return statLoreRenderer.render(customItem.getStatModifiers());
	}

}

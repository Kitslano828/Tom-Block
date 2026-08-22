package org.tomdang.customitemframework;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomItemResolver {

	private final NamespacedKey customItemIDKey;
	private final CustomItemRegistry customItemRegistry;

	public CustomItemResolver(NamespacedKey customItemIDKey, CustomItemRegistry customItemRegistry) {
		this.customItemIDKey = customItemIDKey;
		this.customItemRegistry = customItemRegistry;

	}

	public CustomItem getCustomItem(ItemStack item) {
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			String id = meta.getPersistentDataContainer().get(customItemIDKey, PersistentDataType.STRING);
			return customItemRegistry.getCustomItem(id);
		}
		return null;
	}
}

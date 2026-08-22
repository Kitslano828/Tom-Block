package org.tomdang.customarmorframework;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;

public class CustomArmorResolver extends CustomItemResolver {

	private final CustomArmorRegistry customArmorRegistry;

	public CustomArmorResolver(NamespacedKey customItemIDKey, CustomItemRegistry customItemRegistry, CustomArmorRegistry customArmorRegistry) {
		super(customItemIDKey, customItemRegistry);
		this.customArmorRegistry = customArmorRegistry;
	}

	public CustomArmor getArmor(ItemStack item) {
		CustomItem customItem = super.getCustomItem(item);
		if (customItem == null) {
			return null;
		} else if (isCustomArmor(customItem)) {
			return customArmorRegistry.getArmor(customItem.getId());
		} else {
			return null;
		}
	}

	private boolean isCustomArmor(CustomItem item) {
		return item.getItemCategory().equals(ItemCategory.ARMOR);
	}

}

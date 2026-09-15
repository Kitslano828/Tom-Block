package org.tomdang.customarmorframework;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.tomdang.customitemframework.CustomItemCreator;

public class CustomArmorCreator extends CustomItemCreator {
	public CustomArmorCreator(NamespacedKey customItemIdKey) {
		super(customItemIdKey);
	}

	public ItemStack createItemStack(CustomArmor customArmor) {
		ItemStack itemArmor = super.createItemStack(customArmor);

		ItemMeta meta = itemArmor.getItemMeta();
		if (meta == null) return itemArmor;

		if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
			leatherArmorMeta.setColor(customArmor.getColor());
		}

		meta.lore(renderStatLore(customArmor));

		itemArmor.setItemMeta(meta);

		return itemArmor;
	}
}

package org.tomdang.customarmorframework;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.lore.ItemLoreContext;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

public class CustomArmorCreator extends CustomItemCreator {
	public CustomArmorCreator(NamespacedKey customItemIdKey) {
		super(customItemIdKey);
	}

	public CustomArmorCreator(NamespacedKey customItemIdKey, PlayerStatPresentationRegistry presentations) {
		super(customItemIdKey, presentations);
	}

	public ItemStack createItemStack(CustomArmor customArmor) {
		return createItemStack(customArmor, ItemLoreContext.defaults());
	}

	public ItemStack createItemStack(CustomArmor customArmor, ItemLoreContext context) {
		ItemStack itemArmor = super.createItemStack(customArmor, context);

		ItemMeta meta = itemArmor.getItemMeta();
		if (meta == null) return itemArmor;

		if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
			leatherArmorMeta.setColor(customArmor.getColor());
		}

		itemArmor.setItemMeta(meta);

		return itemArmor;
	}
}

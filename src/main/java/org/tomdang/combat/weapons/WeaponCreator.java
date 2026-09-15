package org.tomdang.combat.weapons;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.lore.ItemLoreContext;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

public class WeaponCreator extends CustomItemCreator {
	public WeaponCreator(NamespacedKey customItemIdKey) {
		super(customItemIdKey);
	}

	public WeaponCreator(NamespacedKey customItemIdKey, PlayerStatPresentationRegistry presentations) {
		super(customItemIdKey, presentations);
	}

	public ItemStack createItemStack(Weapon weapon) {
		return createItemStack(weapon, ItemLoreContext.defaults());
	}

	public ItemStack createItemStack(Weapon weapon, ItemLoreContext context) {
		ItemStack itemTool = super.createItemStack(weapon, context);

		ItemMeta meta = itemTool.getItemMeta();
		if (meta == null) return itemTool;
		meta.setMaxStackSize(1);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemTool.setItemMeta(meta);

		return itemTool;
	}
}

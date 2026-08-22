	package org.tomdang.customarmorframework;

	import net.kyori.adventure.text.Component;
	import org.bukkit.Color;
	import org.bukkit.NamespacedKey;
	import org.bukkit.inventory.ItemStack;
	import org.bukkit.inventory.meta.ItemMeta;
	import org.bukkit.inventory.meta.LeatherArmorMeta;
	import org.tomdang.customitemframework.CustomItemCreator;

	import java.util.ArrayList;
	import java.util.List;

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

			List<Component> lore =  new ArrayList<>(customArmor.getStatsAsComponents());
			meta.lore(lore);

			itemArmor.setItemMeta(meta);

			return itemArmor;
		}
	}

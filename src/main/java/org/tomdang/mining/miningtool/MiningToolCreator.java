package org.tomdang.mining.miningtool;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.lore.ItemLoreContext;

import java.util.List;

public class MiningToolCreator extends CustomItemCreator {

	public MiningToolCreator(NamespacedKey miningToolIdKey) {
		super(miningToolIdKey);
	}

	public ItemStack createItemStack(MiningTool miningTool) {
		return createItemStack(miningTool, ItemLoreContext.defaults());
	}

	public ItemStack createItemStack(MiningTool miningTool, ItemLoreContext context) {
		ItemStack itemTool = super.createItemStack(miningTool, context);

		ItemMeta meta = itemTool.getItemMeta();
		if (meta == null) return itemTool;

		MiniMessage mm = MiniMessage.miniMessage();
		Component breakingPowerLore = mm.deserialize("<dark_gray>Breaking Power: " + miningTool.getBreakingPower())
				.decoration(TextDecoration.ITALIC, false);
		meta.lore(renderLore(miningTool, List.of(breakingPowerLore), context));
		itemTool.setItemMeta(meta);

		return itemTool;
	}
}

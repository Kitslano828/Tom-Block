package org.tomdang.mining.miningtool;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.ItemCategory;

public class MiningToolResolver extends CustomItemResolver {
	MiningToolRegistry miningToolRegistry;

	public MiningToolResolver(NamespacedKey miningToolIdKey, MiningToolRegistry miningToolRegistry, CustomItemRegistry customItemRegistry) {
		super(miningToolIdKey, customItemRegistry);
		this.miningToolRegistry = miningToolRegistry;
	}

	public MiningTool getMiningTool(ItemStack item) {
		CustomItem customItem = super.getCustomItem(item);
		if (customItem == null) {
			return null;
		} else if (isAMiningTool(customItem)) {
			return miningToolRegistry.getMiningTool(customItem.getId());
		} else {
			return null;
		}
	}

	private boolean isAMiningTool(CustomItem item) {
		return item.getItemCategory().equals(ItemCategory.MINING_TOOL);
	}
}

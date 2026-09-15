package org.tomdang.player.stats.menu;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.tomdang.player.stats.presentation.PlayerStatMenuItemFactory;

public class StatsMenuBorderRenderer {
	public void render(Inventory inventory, Material material) {
		if (inventory == null || material == null) throw new IllegalArgumentException("border inputs cannot be null");
		PlayerStatMenuItemFactory factory = new PlayerStatMenuItemFactory();
		for (int slot : new BorderedMenuSlotCalculator().borderSlots())
			inventory.setItem(slot, factory.createFiller(material));
	}
}

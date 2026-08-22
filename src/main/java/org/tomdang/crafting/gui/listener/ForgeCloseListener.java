package org.tomdang.crafting.gui.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.tomdang.crafting.gui.ForgeMenu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ForgeCloseListener implements Listener {

	@EventHandler
	public void onForgeClose(InventoryCloseEvent event) {
		if (!(event.getInventory().getHolder() instanceof ForgeMenu menu)) {
			return;
		}

		Inventory inventory = event.getInventory();
		Player player = (Player) event.getPlayer();

		// Loop through every slot in the GUI
		for (int slot = 0; slot < inventory.getSize(); slot++) {

			// Check if this slot is a designated crafting slot
			if (!menu.isCraftingPosition(slot)) {
				continue;
			}

			ItemStack item = inventory.getItem(slot);

			// Skip empty slots
			if (item == null || item.getType().isAir()) {
				continue;
			}

			// Try to put the item directly into the player's inventory
			HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);

			// If the player's inventory is full, drop leftovers at their feet
			if (!remaining.isEmpty()) {
				for (ItemStack leftover : remaining.values()) {
					player.getWorld().dropItemNaturally(player.getLocation(), leftover);
				}
			}

			// Clear the slot so items don't duplicate
			inventory.setItem(slot, null);
		}
	}

}

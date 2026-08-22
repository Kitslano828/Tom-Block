package org.tomdang.crafting.gui.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.tomdang.TomBlock;
import org.tomdang.crafting.CraftingService;
import org.tomdang.crafting.craftingGrid.CraftingGrid;
import org.tomdang.crafting.gui.ForgeMenu;

import java.util.HashSet;
import java.util.Set;

public class ForgeListener implements Listener {

	private final CraftingService craftingService;
	private final TomBlock instance;

	public ForgeListener(TomBlock instance, CraftingService craftingService) {
		this.instance = instance;
		this.craftingService = craftingService;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// Ensure the clicked inventory is actually your custom ForgeMenu
		if (!(event.getInventory().getHolder() instanceof ForgeMenu menu)) {
			return;
		}

		int clickedSlot = event.getRawSlot();
		if (clickedSlot < 0) return;

		// 1. Cancel ALL shift clicks inside this menu immediately
		if (event.getClick().isShiftClick()) {
			event.setCancelled(true);
			return;
		}

		// Allow players to click and manage items inside their own bottom inventory
		if (event.getClickedInventory() == event.getView().getBottomInventory()) {
			return;
		}
		// If they click a filler or restricted slot, freeze the action
		if (!menu.isCraftingPosition(clickedSlot)) {
			event.setCancelled(true);
		}

		if (menu.isCraftingPosition(clickedSlot)) {
			instance.getServer().getScheduler().runTask(instance, () -> {
				if (event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() != menu) {
					return;
				}
				menu.refreshOutputPreview(craftingService);
			});
		}

		if (menu.isCraftingButton(clickedSlot)) {
			event.setCancelled(true);
			CraftingGrid craftingGrid = menu.createCraftingGrid();
			ItemStack craftedItem = craftingService.craft(craftingGrid);
			if (craftedItem == null) {
				event.getWhoClicked().sendMessage("No matching recipe");
			} else {
				if (event.getWhoClicked() instanceof Player) {
					menu.applyCraftingGrid(craftingGrid);
					// Attempt to add the item and store what couldn't fit
					java.util.HashMap<Integer, ItemStack> leftover = event.getWhoClicked().getInventory().addItem(craftedItem);

					// If the returned map is not empty, it means the inventory was full
					if (!leftover.isEmpty()) {
						for (ItemStack item : leftover.values()) {
							event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), item);
						}
					}
					event.getWhoClicked().sendMessage("You crafted an item!");
					menu.refreshOutputPreview(craftingService);
				}
			}
		}
	}

}


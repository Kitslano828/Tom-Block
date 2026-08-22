package org.tomdang.crafting.gui.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.tomdang.TomBlock;
import org.tomdang.crafting.CraftingService;
import org.tomdang.crafting.gui.ForgeMenu;

public class ForgeDragListener implements Listener {

	private final TomBlock instance;
	private final CraftingService craftingService;

	public ForgeDragListener(TomBlock instance, CraftingService craftingService) {
		this.instance = instance;
		this.craftingService = craftingService;
	}

	@EventHandler
	public void onForgeDrag(InventoryDragEvent event) {
		if (!(event.getInventory().getHolder() instanceof ForgeMenu menu)) {
			return;
		}

		boolean isCraftingPosition = false;

		// Check every slot involved in the drag action
		for (int rawSlot : event.getRawSlots()) {
			// Only check slots belonging to the top inventory
			if (rawSlot < event.getInventory().getSize()) {
				// Cancel the entire drag if even one slot is not a valid crafting position
				if (!menu.isCraftingPosition(rawSlot)) {
					event.setCancelled(true);
					return;
				} else {
					isCraftingPosition = true;
				}
			}
		}

		if (isCraftingPosition) {
			instance.getServer().getScheduler().runTask(instance, () -> {
				if (event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() != menu) {
					return;
				}
				menu.refreshOutputPreview(craftingService);
			});
		}
	}

}

package org.tomdang.collection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class CollectionMenuListener implements Listener {
	@EventHandler public void onClick(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof CollectionMenu) event.setCancelled(true);
	}
	@EventHandler public void onDrag(InventoryDragEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof CollectionMenu) event.setCancelled(true);
	}
}

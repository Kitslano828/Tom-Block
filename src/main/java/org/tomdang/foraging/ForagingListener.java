package org.tomdang.foraging;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class ForagingListener implements Listener {
	private final ForagingService service;
	public ForagingListener(ForagingService service) { this.service = service; }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		if (service.harvest(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.setDropItems(false);
			event.setExpToDrop(0);
		}
	}
}

package org.tomdang.foraging.encounter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

public final class ForagingEncounterListener implements Listener {
	private final ForagingEncounterService service;
	public ForagingEncounterListener(ForagingEncounterService service) { this.service = service; }

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void damage(BlockDamageEvent event) {
		if (service.hit(event.getPlayer(), event.getBlock())) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void protect(BlockBreakEvent event) {
		if (service.isNode(event.getBlock())) {
			event.setCancelled(true);
			event.setDropItems(false);
			event.setExpToDrop(0);
		}
	}
}

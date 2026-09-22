package org.tomdang.foraging;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import java.util.function.Predicate;
import org.bukkit.block.Block;

public final class ForagingListener implements Listener {
	private final ForagingService service;
	private final Predicate<Block> rewardEligible;
	public ForagingListener(ForagingService service, Predicate<Block> rewardEligible) {
		this.service = service;
		this.rewardEligible = rewardEligible;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		if (!rewardEligible.test(event.getBlock())) return;
		if (service.harvest(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.setDropItems(false);
			event.setExpToDrop(0);
		}
	}
}

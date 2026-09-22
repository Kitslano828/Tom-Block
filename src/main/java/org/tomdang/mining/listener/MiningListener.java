package org.tomdang.mining.listener;


import org.tomdang.mining.MiningService;
import org.tomdang.mining.MiningProgressService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.block.Block;
import java.util.function.Predicate;

public class MiningListener implements Listener {

	private final MiningService miningService;
	private final MiningProgressService progressService;
	private final Predicate<Block> rewardEligible;

	public MiningListener(MiningService miningService, MiningProgressService progressService, Predicate<Block> rewardEligible) {
		this.miningService = miningService;
		this.progressService = progressService;
		this.rewardEligible = rewardEligible;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event) {
		if (!rewardEligible.test(event.getBlock())) return;
		progressService.start(event.getPlayer(), event.getBlock());
	}

	@EventHandler
	public void onBlockDamageAbort(BlockDamageAbortEvent event) {
		progressService.stop(event.getPlayer(), event.getBlock());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		progressService.stop(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!rewardEligible.test(event.getBlock())) return;
		if (progressService.interceptBreak(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			return;
		}
		miningService.blockBreak(event, event.getPlayer().getUniqueId());
	}


}

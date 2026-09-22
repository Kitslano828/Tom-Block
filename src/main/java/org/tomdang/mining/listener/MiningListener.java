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

public class MiningListener implements Listener {

	private final MiningService miningService;
	private final MiningProgressService progressService;

	public MiningListener(MiningService miningService, MiningProgressService progressService) {
		this.miningService = miningService;
		this.progressService = progressService;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event) {
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
		if (progressService.interceptBreak(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			return;
		}
		miningService.blockBreak(event, event.getPlayer().getUniqueId());
	}


}

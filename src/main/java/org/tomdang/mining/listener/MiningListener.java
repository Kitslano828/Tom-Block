package org.tomdang.mining.listener;


import org.tomdang.mining.MiningService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {

	private final MiningService miningService;

	public MiningListener(MiningService miningService) {
		this.miningService = miningService;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		miningService.blockBreak(event, event.getPlayer().getUniqueId());
	}


}

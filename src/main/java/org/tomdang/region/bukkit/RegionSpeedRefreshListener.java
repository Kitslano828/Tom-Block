package org.tomdang.region.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tomdang.player.movement.PlayerMovementSpeedRefreshScheduler;

public final class RegionSpeedRefreshListener implements Listener {
	private final PlayerMovementSpeedRefreshScheduler scheduler;

	public RegionSpeedRefreshListener(PlayerMovementSpeedRefreshScheduler scheduler) {
		if (scheduler == null) throw new IllegalArgumentException("scheduler cannot be null");
		this.scheduler = scheduler;
	}

	@EventHandler
	public void onRegionTransition(PlayerRegionTransitionEvent event) {
		scheduler.requestRefresh(event.getPlayer());
	}
}

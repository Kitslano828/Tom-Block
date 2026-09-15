package org.tomdang.customitemframework.refresh.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.tomdang.customitemframework.refresh.PlayerItemRefreshScheduler;

public final class PlayerHeldItemRefreshListener implements Listener {

	private final PlayerItemRefreshScheduler refreshScheduler;

	public PlayerHeldItemRefreshListener(PlayerItemRefreshScheduler refreshScheduler) {
		if (refreshScheduler == null) throw new IllegalArgumentException("refreshScheduler cannot be null");
		this.refreshScheduler = refreshScheduler;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHeldSlotChange(PlayerItemHeldEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSwapHands(PlayerSwapHandItemsEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}
}

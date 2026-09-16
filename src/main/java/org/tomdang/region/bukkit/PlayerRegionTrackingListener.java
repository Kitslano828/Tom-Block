package org.tomdang.region.bukkit;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.tomdang.region.tracking.PlayerRegionTrackingService;

public final class PlayerRegionTrackingListener implements Listener {
	private final PlayerRegionTrackingService tracking;
	private final BukkitBlockPositionAdapter positions;
	private final RegionTrackingDebugService debug;

	public PlayerRegionTrackingListener(PlayerRegionTrackingService tracking, BukkitBlockPositionAdapter positions,
			RegionTrackingDebugService debug) {
		this.tracking = tracking;
		this.positions = positions;
		this.debug = debug;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		tracking.update(event.getPlayer().getUniqueId(), positions.fromLocation(event.getPlayer().getLocation()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		if (to != null) tracking.update(event.getPlayer().getUniqueId(), positions.fromLocation(to));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		Location to = event.getTo();
		if (to != null) tracking.update(event.getPlayer().getUniqueId(), positions.fromLocation(to));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChangedWorld(PlayerChangedWorldEvent event) {
		tracking.update(event.getPlayer().getUniqueId(), positions.fromLocation(event.getPlayer().getLocation()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent event) {
		tracking.update(event.getPlayer().getUniqueId(), positions.fromLocation(event.getRespawnLocation()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		tracking.clear(event.getPlayer().getUniqueId());
		debug.clear(event.getPlayer().getUniqueId());
	}
}

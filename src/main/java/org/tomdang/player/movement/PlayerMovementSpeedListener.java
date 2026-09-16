package org.tomdang.player.movement;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class PlayerMovementSpeedListener implements Listener {
	private final PlayerMovementSpeedRefreshScheduler refreshScheduler;

	public PlayerMovementSpeedListener(PlayerMovementSpeedRefreshScheduler refreshScheduler) {
		if (refreshScheduler == null) throw new IllegalArgumentException("refreshScheduler cannot be null");
		this.refreshScheduler = refreshScheduler;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHeldSlotChange(PlayerItemHeldEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSwapHands(PlayerSwapHandItemsEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onArmorChange(PlayerArmorChangeEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player player) refreshScheduler.requestRefresh(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player player) refreshScheduler.requestRefresh(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPickup(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player player) refreshScheduler.requestRefresh(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldChange(PlayerChangedWorldEvent event) {
		refreshScheduler.requestRefresh(event.getPlayer());
	}
}

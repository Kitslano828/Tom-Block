package org.tomdang.island;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PrivateIslandWorldListener implements Listener {
	static final int TRAVEL_RADIUS = 192;
	private final Plugin plugin;
	private final PrivateIslandWorldService worlds;
	private final Map<UUID, Long> lastWarning = new HashMap<>();
	public PrivateIslandWorldListener(Plugin plugin, PrivateIslandWorldService worlds) { this.plugin = plugin; this.worlds = worlds; }
	@EventHandler public void onWorldChange(PlayerChangedWorldEvent event) { scheduleCheck(event.getFrom()); }
	@EventHandler public void onQuit(PlayerQuitEvent event) { scheduleCheck(event.getPlayer().getWorld()); }
	@EventHandler public void onMove(PlayerMoveEvent event) {
		if (!event.hasChangedBlock() || !worlds.isPrivateIsland(event.getTo().getWorld()) || contains(event.getTo())) return;
		event.setTo(contains(event.getFrom()) ? event.getFrom() : event.getPlayer().getWorld().getSpawnLocation());
		long now = System.currentTimeMillis();
		if (now - lastWarning.getOrDefault(event.getPlayer().getUniqueId(), 0L) >= 2000) {
			lastWarning.put(event.getPlayer().getUniqueId(), now);
			event.getPlayer().sendActionBar(Component.text("The open sea is too dangerous to travel farther.", NamedTextColor.RED));
		}
	}
	private void scheduleCheck(World world) {
		Bukkit.getScheduler().runTaskLater(plugin, () -> worlds.scheduleUnload(world), 1L);
	}
	static boolean contains(org.bukkit.Location location) {
		long x = location.getBlockX();
		long z = location.getBlockZ();
		return x * x + z * z <= (long) TRAVEL_RADIUS * TRAVEL_RADIUS;
	}
}

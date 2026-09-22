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
import org.tomdang.island.runtime.IslandContextService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PrivateIslandWorldListener implements Listener {
	private final Plugin plugin;
	private final PrivateIslandWorldService worlds;
	private final IslandContextService contexts;
	private final Map<UUID, Long> lastWarning = new HashMap<>();
	public PrivateIslandWorldListener(Plugin plugin, PrivateIslandWorldService worlds, IslandContextService contexts) {
		this.plugin = plugin;
		this.worlds = worlds;
		this.contexts = contexts;
	}
	@EventHandler public void onWorldChange(PlayerChangedWorldEvent event) { scheduleCheck(event.getFrom()); }
	@EventHandler public void onQuit(PlayerQuitEvent event) { scheduleCheck(event.getPlayer().getWorld()); }
	@EventHandler public void onMove(PlayerMoveEvent event) {
		if (!event.hasChangedBlock() || !worlds.isPrivateIsland(event.getTo().getWorld())) return;
		int radius = contexts.runtime(event.getTo().getWorld().getName()).orElseThrow().preset().travelRadius();
		if (contains(event.getTo(), radius)) return;
		event.setTo(contains(event.getFrom(), radius) ? event.getFrom() : event.getPlayer().getWorld().getSpawnLocation());
		long now = System.currentTimeMillis();
		if (now - lastWarning.getOrDefault(event.getPlayer().getUniqueId(), 0L) >= 2000) {
			lastWarning.put(event.getPlayer().getUniqueId(), now);
			event.getPlayer().sendActionBar(Component.text("The open sea is too dangerous to travel farther.", NamedTextColor.RED));
		}
	}
	private void scheduleCheck(World world) {
		Bukkit.getScheduler().runTaskLater(plugin, () -> worlds.scheduleUnload(world), 1L);
	}
	static boolean contains(org.bukkit.Location location, int radius) {
		long x = location.getBlockX();
		long z = location.getBlockZ();
		return x * x + z * z <= (long) radius * radius;
	}
}

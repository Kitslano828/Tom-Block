package org.tomdang.island;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public final class PrivateIslandWorldService {
	private final Plugin plugin;
	private final Map<String, Integer> unloadTasks = new HashMap<>();
	public PrivateIslandWorldService(Plugin plugin) { this.plugin = plugin; }

	public World load(PrivateIsland island) {
		cancelUnload(island.worldName());
		World loaded = Bukkit.getWorld(island.worldName());
		if (loaded != null) return loaded;
		World world = new WorldCreator(island.worldName()).generator(new StarterIslandGenerator()).createWorld();
		if (world == null) throw new IllegalStateException("Paper could not load island world " + island.worldName());
		world.setSpawnLocation(new Location(world, 0.5, 66, 0.5));
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		world.setGameRule(GameRule.DO_FIRE_TICK, false);
		world.setAutoSave(true);
		// Travel is enforced invisibly by PrivateIslandWorldListener. Keep the
		// vanilla border far away so its blue wall is never part of the view.
		world.getWorldBorder().setCenter(0, 0);
		world.getWorldBorder().setSize(59_999_968);
		return world;
	}

	public void scheduleUnload(World world) {
		if (!isPrivateIsland(world) || !world.getPlayers().isEmpty()) return;
		cancelUnload(world.getName());
		int task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
			unloadTasks.remove(world.getName());
			if (world.getPlayers().isEmpty()) Bukkit.unloadWorld(world, true);
		}, 20L * 60L).getTaskId();
		unloadTasks.put(world.getName(), task);
	}

	public boolean isPrivateIsland(World world) { return world != null && world.getName().startsWith("island_"); }
	private void cancelUnload(String worldName) {
		Integer task = unloadTasks.remove(worldName);
		if (task != null) Bukkit.getScheduler().cancelTask(task);
	}
}

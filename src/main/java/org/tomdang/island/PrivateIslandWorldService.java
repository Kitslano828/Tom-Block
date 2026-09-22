package org.tomdang.island;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;
import org.tomdang.island.preset.IslandPreset;
import org.tomdang.island.preset.IslandPresetRegistry;
import org.tomdang.island.runtime.IslandContextService;

import java.util.HashMap;
import java.util.Map;

public final class PrivateIslandWorldService {
	private final Plugin plugin;
	private final IslandPresetRegistry presets;
	private final IslandContextService contexts;
	private final Map<String, Integer> unloadTasks = new HashMap<>();
	public PrivateIslandWorldService(Plugin plugin, IslandPresetRegistry presets, IslandContextService contexts) {
		this.plugin = plugin;
		this.presets = presets;
		this.contexts = contexts;
	}

	public World load(PrivateIsland island) {
		cancelUnload(island.worldName());
		IslandPreset preset = presets.require(island.presetKey());
		contexts.register(island, preset);
		World loaded = Bukkit.getWorld(island.worldName());
		if (loaded != null) return loaded;
		if (!"STRANDED_OCEAN".equals(preset.generator()))
			throw new IllegalArgumentException("Unsupported private island generator: " + preset.generator());
		World world = new WorldCreator(island.worldName()).generator(new StarterIslandGenerator()).createWorld();
		if (world == null) throw new IllegalStateException("Paper could not load island world " + island.worldName());
		world.setSpawnLocation(new Location(world, preset.spawnX(), preset.spawnY(), preset.spawnZ()));
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
		IslandPreset preset = contexts.runtime(world.getName()).orElseThrow().preset();
		cancelUnload(world.getName());
		int task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
			unloadTasks.remove(world.getName());
			if (world.getPlayers().isEmpty() && Bukkit.unloadWorld(world, true)) contexts.unregister(world.getName());
		}, 20L * preset.unloadDelaySeconds()).getTaskId();
		unloadTasks.put(world.getName(), task);
	}

	public boolean isPrivateIsland(World world) {
		return world != null && contexts.runtime(world.getName()).map(runtime -> runtime.preset().privateWorld()).orElse(false);
	}
	private void cancelUnload(String worldName) {
		Integer task = unloadTasks.remove(worldName);
		if (task != null) Bukkit.getScheduler().cancelTask(task);
	}
}

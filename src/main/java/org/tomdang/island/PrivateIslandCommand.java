package org.tomdang.island;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PrivateIslandCommand implements CommandExecutor {
	private final PrivateIslandService islands;
	private final PrivateIslandWorldService worlds;
	private final Plugin plugin;
	public PrivateIslandCommand(Plugin plugin, PrivateIslandService islands, PrivateIslandWorldService worlds) {
		this.plugin = plugin; this.islands = islands; this.worlds = worlds;
	}
	@Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
		if (args.length > 0 && args[0].equalsIgnoreCase("leave")) {
			World destination = Bukkit.getWorlds().stream().filter(world -> !worlds.isPrivateIsland(world)).findFirst().orElse(null);
			if (destination == null) { player.sendMessage("§cNo public world is available."); return true; }
			World previous = player.getWorld();
			player.teleportAsync(destination.getSpawnLocation()).thenRun(() ->
					Bukkit.getScheduler().runTaskLater(plugin, () -> worlds.scheduleUnload(previous), 1L));
			return true;
		}
		player.sendMessage("§7Preparing your private island...");
		PrivateIsland island = islands.getOrCreate(player.getUniqueId());
		World world = worlds.load(island);
		player.teleportAsync(world.getSpawnLocation());
		return true;
	}
}

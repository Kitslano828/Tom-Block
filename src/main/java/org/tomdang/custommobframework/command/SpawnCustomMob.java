package org.tomdang.custommobframework.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.JellyfishColor;

import java.util.ArrayList;
import java.util.List;

public class SpawnCustomMob implements CommandExecutor, TabCompleter {

	private final CustomMobRegistry customMobRegistry;

	public SpawnCustomMob(CustomMobRegistry customMobRegistry) {
		this.customMobRegistry = customMobRegistry;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("YOU ARE NOT A PLAYER");
			return true;
		}

		if (args.length < 1 || args.length > 2) {
			sender.sendMessage("§f§lUSAGE: /spawncustommob <name> [color]");
			return true;
		}

		Location spawnLocation = ((Player) sender).getLocation();

		String mobID = args[0].toUpperCase(java.util.Locale.ROOT);
		CustomMob customMob = customMobRegistry.getCustomMob(mobID);
		if(customMob == null) {
			sender.sendMessage("NOT A VALID MOB!");
			return true;
		}
		JellyfishColor color = null;
		if (args.length == 2) {
			if (!"JELLYFISH".equals(mobID)) {
				sender.sendMessage("Color is currently supported only for Jellyfish.");
				return true;
			}
			color = JellyfishColor.parse(args[1]).orElse(null);
			if (color == null) {
				sender.sendMessage("Unknown jellyfish color. Use blue, green, orange, or pink.");
				return true;
			}
		}
		Entity mob = customMobRegistry.getCustomMobAsMob(customMob, spawnLocation,
				color == null ? null : color.name());
		if (mob == null) {
			sender.sendMessage("This mob cannot spawn in your current region.");
			return true;
		}
		sender.sendMessage("Successfully spawned " + (color == null ? "" : color.name().toLowerCase(java.util.Locale.ROOT) + " ")
				+ customMob.getName() + " at your location!");

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		List<String> mobList = customMobRegistry.getCustomMobsAsList();
		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			for (String id : mobList) {
				completions.add(id);
			}
			return completions;
		}
		if (args.length == 2 && "JELLYFISH".equalsIgnoreCase(args[0])) {
			return java.util.Arrays.stream(JellyfishColor.values())
					.map(color -> color.name().toLowerCase(java.util.Locale.ROOT))
					.filter(color -> color.startsWith(args[1].toLowerCase(java.util.Locale.ROOT)))
					.toList();
		}

		return List.of();
	}
}

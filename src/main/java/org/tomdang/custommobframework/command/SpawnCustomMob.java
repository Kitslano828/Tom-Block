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

		if (args.length != 1) {
			sender.sendMessage("§f§lUSAGE: /spawncustommob <name>");
			return true;
		}

		Location spawnLocation = ((Player) sender).getLocation();

		String mobID = args[0];
		CustomMob customMob = customMobRegistry.getCustomMob(mobID);
		if(customMob == null) {
			sender.sendMessage("NOT A VALID MOB!");
			return true;
		}
		Entity mob = customMobRegistry.getCustomMobAsMob(customMob, spawnLocation);

		sender.sendMessage("Successfully spawned " + customMob.getName() + "at you location!");

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

		return List.of();
	}
}

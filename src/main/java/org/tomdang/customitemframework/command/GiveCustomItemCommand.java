package org.tomdang.customitemframework.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemStackFactory;

import java.util.List;

public final class GiveCustomItemCommand implements CommandExecutor, TabCompleter {
	private final CustomItemRegistry registry;
	private final CustomItemStackFactory stacks;

	public GiveCustomItemCommand(CustomItemRegistry registry, CustomItemStackFactory stacks) {
		if (registry == null || stacks == null) throw new IllegalArgumentException("registry and stacks are required");
		this.registry = registry;
		this.stacks = stacks;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
	                         @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command requires a player.");
			return true;
		}
		if (args.length != 1) {
			player.sendMessage("Usage: /givecustomitem <id>");
			return true;
		}
		CustomItem item = registry.getCustomItem(args[0].toUpperCase(java.util.Locale.ROOT));
		if (item == null) {
			player.sendMessage("Unknown custom item: " + args[0]);
			return true;
		}
		var leftovers = player.getInventory().addItem(stacks.createCustomItemStack(item));
		if (!leftovers.isEmpty()) {
			player.sendMessage("No free inventory slot for " + item.getId() + ".");
			return true;
		}
		player.sendMessage("Given " + item.getId() + ".");
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
	                                 @NotNull String label, @NotNull String @NotNull [] args) {
		if (args.length != 1) return List.of();
		String prefix = args[0].toUpperCase(java.util.Locale.ROOT);
		return registry.getItemIds().stream().filter(id -> id.startsWith(prefix)).toList();
	}
}

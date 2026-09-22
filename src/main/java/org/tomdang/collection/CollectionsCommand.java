package org.tomdang.collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CollectionsCommand implements CommandExecutor {
	private final CollectionService service;
	public CollectionsCommand(CollectionService service) { this.service = service; }
	@Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player player)) { sender.sendMessage("This command requires a player."); return true; }
		player.openInventory(new CollectionMenu(player, service).getInventory());
		return true;
	}
}

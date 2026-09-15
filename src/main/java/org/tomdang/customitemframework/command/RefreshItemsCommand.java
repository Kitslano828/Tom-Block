package org.tomdang.customitemframework.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshResult;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshService;

public final class RefreshItemsCommand implements CommandExecutor {

	private final PlayerInventoryItemRefreshService refreshService;

	public RefreshItemsCommand(PlayerInventoryItemRefreshService refreshService) {
		if (refreshService == null) throw new IllegalArgumentException("refreshService cannot be null");
		this.refreshService = refreshService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
	                         @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by a player.");
			return true;
		}
		if (args.length != 0) {
			player.sendMessage("Usage: /refreshitems");
			return true;
		}

		PlayerInventoryItemRefreshResult result = refreshService.refresh(player);
		player.sendMessage("Item refresh complete: inspected " + result.inspected()
				+ ", updated " + result.updated()
				+ ", skipped " + result.skipped()
				+ ", failed " + result.failed() + ".");
		return true;
	}
}

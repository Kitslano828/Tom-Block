package org.tomdang.crafting.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.crafting.gui.ForgeMenu;

public class ForgeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player)) {
			System.out.println("You aren't a player!");
			return true;
		}

		ForgeMenu menu = new ForgeMenu();

		menu.open((Player) sender);

		return true;
	}
}

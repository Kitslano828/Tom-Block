package org.tomdang.player.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.player.playerresource.PlayerResourceService;

public class HealCommand implements CommandExecutor {

	private final PlayerResourceService playerResourceService;

	public HealCommand(PlayerResourceService playerResourceService) {
		this.playerResourceService = playerResourceService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player)) {
			System.out.println("YOU ARE NOT A PLAYER!");
			return true;
		}

		String healAmount = args[0];
		double heal;

		try {
			heal = Double.parseDouble(healAmount);
			if (heal < 0) {
				sender.sendMessage("heal cannot be below 0!");
				return true;
			}
		} catch (NumberFormatException exception) {
			sender.sendMessage("Not a valid number!");
			return true;
		}

		playerResourceService.heal((Player) sender, heal);
		sender.sendMessage("You have been healed for " + heal + " health!");

		return true;
	}
}

package org.tomdang.player.command.energy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.player.playerresource.PlayerResourceService;

public class UseEnergy implements CommandExecutor {

	private final PlayerResourceService playerResourceService;

	public UseEnergy(PlayerResourceService playerResourceService) {
		this.playerResourceService = playerResourceService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player)) {
			System.out.println("YOU ARE NOT A PLAYER!");
			return true;
		}

		String energyAmount = args[0];
		double energySpent;

		try {
			energySpent = Double.parseDouble(energyAmount);
			if (energySpent < 0) {
				sender.sendMessage("energy cannot be below 0!");
				return true;
			}
		} catch (NumberFormatException exception) {
			sender.sendMessage("Not a valid number!");
			return true;
		}

		if (playerResourceService.spendEnergy((Player) sender, energySpent)) {
			sender.sendMessage("[TIRING] You spent " + energySpent + " energy!");
		} else {
			return true;
		}
		return true;
	}
}

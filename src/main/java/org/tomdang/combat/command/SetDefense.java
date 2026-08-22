package org.tomdang.combat.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;

public class SetDefense implements CommandExecutor {

	private final PlayerProfileService playerProfileService;

	public SetDefense(PlayerProfileService playerProfileService) {
		this.playerProfileService = playerProfileService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player)) {
			System.out.println("YOU ARE NOT A PLAYER!");
			return true;
		}

		String defenseAmount = args[0];
		double defense;

		Player player = (Player) sender;

		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());

		try {
			defense = Double.parseDouble(defenseAmount);
			if (defense < 0) {
				player.sendMessage("Defense cannot be below 0!");
				return true;
			}
		} catch (NumberFormatException exception) {
			sender.sendMessage("Not a valid number!");
			return true;
		}
		playerProfile.setDefense(defense);
		player.sendMessage("Set your defense to " + defense + "!");
		return true;
	}
}
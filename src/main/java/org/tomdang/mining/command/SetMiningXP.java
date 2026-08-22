package org.tomdang.mining.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;

import java.util.ArrayList;
import java.util.List;

public class SetMiningXP implements CommandExecutor, TabCompleter {

	PlayerProfileService playerProfileService;

	public SetMiningXP(PlayerProfileService playerProfileService) {
		this.playerProfileService = playerProfileService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (args.length != 2) {
			sender.sendMessage("§f§lUSAGE: /setminingxp <name> <amount>");
			return true;
		}

		String playerName = args[0];
		String xpText = args[1];

		Player target = Bukkit.getPlayerExact(playerName);

		if (target == null) {
			sender.sendMessage("PLAYER NOT ONLINE");
			return true;
		}

		PlayerProfile targetProfile = playerProfileService.getPlayerProfileFromMap(target.getUniqueId());

		int xp;

		try {
			xp = Integer.parseInt(xpText);
			if (xp < 0) {
				sender.sendMessage("XP CANNOT BE BELOW 0");
				return true;
			}
		} catch (NumberFormatException exception) {
			sender.sendMessage("§cThe mining xp must be a whole number.");
			return true;
		}

		targetProfile.setMiningXP(xp);


		sender.sendMessage("§a§lSet " + playerName + "'s Mining xp to " + xp + "!");

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			for (Player p : Bukkit.getOnlinePlayers()) {
				completions.add(p.getName());
			}
			return completions;
		}
		return List.of();
	}
}

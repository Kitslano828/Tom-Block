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

public class GiveMiningLevel implements CommandExecutor, TabCompleter {

	private final PlayerProfileService playerProfileService;

	public GiveMiningLevel(PlayerProfileService playerProfileService) {
		this.playerProfileService = playerProfileService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (args.length != 1) {
			sender.sendMessage("§f§lUSAGE: /getmininglvl <name>");
			return true;
		}

		String playerName = args[0];
		Player target = Bukkit.getPlayerExact(playerName);

		if (target == null) {
			sender.sendMessage("PLAYER NOT ONLINE");
			return true;
		}

		PlayerProfile targetProfile = playerProfileService.getPlayerProfileFromMap(target.getUniqueId());

		sender.sendMessage("§a§lPlayer" + playerName + "'s mining level is " + targetProfile.getMiningLVL() + "!");

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

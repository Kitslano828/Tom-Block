package org.tomdang.player.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.menu.PlayerStatsOverviewMenu;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;
import org.tomdang.player.stats.presentation.PlayerStatsOverviewConfiguration;

public class PlayerStatsCommand implements CommandExecutor {

	private final PlayerStatsService playerStatsService;
	private final PlayerStatPresentationRegistry presentations;
	private final PlayerStatsOverviewConfiguration overviewConfiguration;

	public PlayerStatsCommand(PlayerStatsService playerStatsService, PlayerStatPresentationRegistry presentations,
	                          PlayerStatsOverviewConfiguration overviewConfiguration) {
		if (playerStatsService == null || presentations == null || overviewConfiguration == null) throw new IllegalArgumentException("command inputs cannot be null");
		this.playerStatsService = playerStatsService;
		this.presentations = presentations;
		this.overviewConfiguration = overviewConfiguration;
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) return true;
		PlayerStatsOverviewMenu menu = new PlayerStatsOverviewMenu(overviewConfiguration, presentations, playerStatsService.evaluate(player));
		player.openInventory(menu.getInventory());

		return true;
	}

}

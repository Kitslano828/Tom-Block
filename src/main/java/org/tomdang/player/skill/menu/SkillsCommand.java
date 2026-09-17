package org.tomdang.player.skill.menu;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

public final class SkillsCommand implements CommandExecutor {
	private final PlayerProfileService profiles;
	private final SkillMenuConfiguration configuration;
	private final PlayerStatPresentationRegistry statPresentations;

	public SkillsCommand(PlayerProfileService profiles, SkillMenuConfiguration configuration, PlayerStatPresentationRegistry statPresentations) {
		if (profiles == null || configuration == null || statPresentations == null) throw new IllegalArgumentException("Command dependencies are required");
		this.profiles = profiles;
		this.configuration = configuration;
		this.statPresentations = statPresentations;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
	                         @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by a player.");
			return true;
		}
		PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
		if (profile == null) {
			player.sendMessage("Your player profile is not loaded.");
			return true;
		}
		player.openInventory(new SkillsOverviewMenu(configuration, profile, statPresentations).getInventory());
		return true;
	}
}

package org.tomdang.player.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.PlayerStatValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SetStatCommand implements CommandExecutor, TabCompleter {

	private final PlayerProfileService playerProfileService;

	public SetStatCommand(PlayerProfileService playerProfileService) {
		if (playerProfileService == null) throw new IllegalArgumentException("playerProfileService cannot be null");
		this.playerProfileService = playerProfileService;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
	                         @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by a player.");
			return true;
		}

		PlayerProfile profile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		if (profile == null) {
			player.sendMessage("Your player profile is not loaded.");
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
			profile.resetAllStats();
			player.sendMessage("Reset all base stats to their default values.");
			return true;
		}

		if (args.length != 2) {
			player.sendMessage("Usage: /setstat <stat> <amount> or /setstat reset");
			return true;
		}

		PlayerStatType statType = resolveStatType(args[0]);
		if (statType == null) {
			player.sendMessage("Unknown stat '" + args[0] + "'. Use tab completion to see available stats.");
			return true;
		}

		double amount;
		try {
			amount = Double.parseDouble(args[1]);
		} catch (NumberFormatException exception) {
			player.sendMessage("Amount must be a valid number.");
			return true;
		}

		if (!Double.isFinite(amount)) {
			player.sendMessage("Amount must be a finite number.");
			return true;
		}
		if (amount < statType.getMinimumValue()) {
			player.sendMessage(statType.getDisplayName() + " cannot be below "
					+ PlayerStatValueFormatter.format(statType.getMinimumValue()) + ".");
			return true;
		}

		profile.setStat(statType, amount);
		player.sendMessage("Set base " + statType.getDisplayName() + " to "
				+ PlayerStatValueFormatter.format(amount) + ".");
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
	                                            @NotNull String alias, @NotNull String[] args) {
		if (args.length != 1) return List.of();

		String prefix = args[0].toLowerCase(Locale.ROOT);
		List<String> suggestions = new ArrayList<>();
		if ("reset".startsWith(prefix)) suggestions.add("reset");
		Arrays.stream(PlayerStatType.values())
				.map(PlayerStatType::getStorageKey)
				.filter(stat -> stat.toLowerCase(Locale.ROOT).startsWith(prefix))
				.forEach(suggestions::add);
		return List.copyOf(suggestions);
	}

	private PlayerStatType resolveStatType(String input) {
		String normalized = input.trim().toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
		return Arrays.stream(PlayerStatType.values())
				.filter(statType -> statType.getStorageKey().equalsIgnoreCase(normalized)
						|| statType.name().toLowerCase(Locale.ROOT).replace('_', '-').equals(normalized)
						|| statType.getDisplayName().toLowerCase(Locale.ROOT).replace(' ', '-').equals(normalized))
				.findFirst()
				.orElse(null);
	}
}

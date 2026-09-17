package org.tomdang.player.skill.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.skill.SkillProgress;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.skill.SkillXpCurve;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Sets the sender's total skill XP, directly or through a level threshold. */
public final class SetSkillCommand implements CommandExecutor, TabCompleter {
	private final PlayerProfileService profiles;

	public SetSkillCommand(PlayerProfileService profiles) {
		if (profiles == null) throw new IllegalArgumentException("profiles cannot be null");
		this.profiles = profiles;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
	                         @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by a player.");
			return true;
		}
		if (args.length != 3) {
			player.sendMessage("Usage: /setskill <skill> <lvl|xp> <amount>");
			return true;
		}
		SkillType skill;
		try {
			skill = SkillType.valueOf(args[0].toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			player.sendMessage("Unknown skill '" + args[0] + "'. Use tab completion to see available skills.");
			return true;
		}
		String mode = args[1].toLowerCase(Locale.ROOT);
		if (!mode.equals("lvl") && !mode.equals("xp")) {
			player.sendMessage("Choose 'lvl' or 'xp'. Usage: /setskill <skill> <lvl|xp> <amount>");
			return true;
		}
		long totalXp;
		try {
			if (mode.equals("lvl")) {
				int level = Integer.parseInt(args[2]);
				if (level < 0 || level > SkillXpCurve.MAX_LEVEL) {
					player.sendMessage("Level must be between 0 and " + SkillXpCurve.MAX_LEVEL + ".");
					return true;
				}
				totalXp = SkillXpCurve.totalXpForLevel(level);
			} else {
				totalXp = Long.parseLong(args[2]);
				if (totalXp < 0) {
					player.sendMessage("XP cannot be below 0.");
					return true;
				}
			}
		} catch (NumberFormatException exception) {
			player.sendMessage("Amount must be a whole number.");
			return true;
		}
		PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
		if (profile == null) {
			player.sendMessage("Your player profile is not loaded.");
			return true;
		}
		skill.setTotalXp(profile, totalXp);
		SkillProgress result = skill.progress(profile);
		player.sendMessage("Set " + skill.name().toLowerCase(Locale.ROOT) + " to level " + result.level()
				+ " (" + result.totalXp() + " total XP).");
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
	                                             @NotNull String alias, @NotNull String[] args) {
		List<String> choices = switch (args.length) {
			case 1 -> Arrays.stream(SkillType.values()).map(skill -> skill.name().toLowerCase(Locale.ROOT)).toList();
			case 2 -> List.of("lvl", "xp");
			default -> List.of();
		};
		if (args.length < 1 || args.length > 2) return List.of();
		String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
		return choices.stream().filter(choice -> choice.startsWith(prefix)).toList();
	}
}

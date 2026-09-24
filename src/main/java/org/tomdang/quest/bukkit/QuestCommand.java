package org.tomdang.quest.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.progress.QuestProgress;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.progress.QuestSignal;

public final class QuestCommand implements CommandExecutor {
	private final QuestProgressService quests;

	public QuestCommand(QuestProgressService quests) { this.quests = quests; }

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) { sender.sendMessage("This command requires a player."); return true; }
		if (args.length == 0 || args[0].equalsIgnoreCase("inspect")) {
			if (quests.progress(player.getUniqueId()).isEmpty()) player.sendMessage("No quest progress loaded.");
			for (QuestProgress progress : quests.progress(player.getUniqueId()))
				player.sendMessage(progress.questId() + ": " + progress.status() + " / " + progress.currentStageId());
			return true;
		}
		if (!player.hasPermission("tomblock.admin.quest")) {
			player.sendMessage("You do not have permission to modify quests."); return true;
		}
		try {
			switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
				case "start" -> quests.start(player.getUniqueId(), require(args, 1));
				case "complete" -> quests.complete(player.getUniqueId(), require(args, 1));
				case "reset" -> quests.reset(player.getUniqueId(), require(args, 1));
				case "branch" -> quests.chooseBranch(player.getUniqueId(), require(args, 1), require(args, 2));
				case "signal" -> quests.signal(player.getUniqueId(), QuestSignal.one(
						QuestObjectiveType.valueOf(require(args, 1).toUpperCase(java.util.Locale.ROOT)), require(args, 2)));
				default -> { return false; }
			}
			player.sendMessage("Quest command completed.");
		} catch (RuntimeException exception) {
			player.sendMessage("Quest command failed: " + exception.getMessage());
		}
		return true;
	}

	private static String require(String[] args, int index) {
		if (args.length <= index || args[index].isBlank()) throw new IllegalArgumentException("Missing command argument");
		return args[index];
	}
}

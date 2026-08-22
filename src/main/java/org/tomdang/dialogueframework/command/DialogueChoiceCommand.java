package org.tomdang.dialogueframework.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;

import java.util.UUID;

public class DialogueChoiceCommand implements CommandExecutor {

	private final DialogueSessionService dialogueSessionService;
	private final DialogueController dialogueController;

	public DialogueChoiceCommand(DialogueSessionService dialogueSessionService, DialogueController dialogueController) {
		if (dialogueSessionService == null) throw new IllegalArgumentException("dialogueSessionService cannot be null");
		if (dialogueController == null) throw new IllegalArgumentException("dialogueController cannot be null");

		this.dialogueSessionService = dialogueSessionService;
		this.dialogueController = dialogueController;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

		if (!(sender instanceof Player player)) return true;

		if (args.length != 2) return true;

		String displayedNodeID = args[0];
		String choiceID = args[1];

		UUID playerUUID = player.getUniqueId();
		DialogueSession session = dialogueSessionService.getActiveSession(playerUUID);
		if (session == null) return true;
		if (!displayedNodeID.equals(session.getCurrentNodeID())) return true;

		dialogueController.selectChoice(player, choiceID);

		return true;
	}
}

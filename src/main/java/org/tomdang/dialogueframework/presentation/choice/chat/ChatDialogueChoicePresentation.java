package org.tomdang.dialogueframework.presentation.choice.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.choice.DialogueChoicePresentation;
import org.tomdang.dialogueframework.session.DialogueSession;

import java.util.List;

public class ChatDialogueChoicePresentation implements DialogueChoicePresentation {

	@Override
	public void showChoices(Player player, DialogueSession session) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (session == null) throw new IllegalArgumentException("session cannot be null");

		if (!player.getUniqueId().equals(session.getPlayerUUID())) throw new IllegalStateException("Player UUIDs do not match");

		DialogueNode node = session.getCurrentNode();
		if (node == null) throw new IllegalStateException("Node is unexpected null");
		List<DialogueChoice> dialogueChoices = node.getDialogueChoices();

		if (dialogueChoices.isEmpty()) return;

		player.sendMessage(Component.text("[CHOICE] Choose a response" ));

		for (DialogueChoice choice : dialogueChoices) {
			player.sendMessage(
					Component.text("[ " + choice.getDisplayText() + " ]", NamedTextColor.YELLOW)
							// Adds the click event to the component
							.clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(
									"/dialoguechoice " + node.getNodeID() + " " + choice.getChoiceID()
							))
							.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
									Component.text("Click to choose this response", NamedTextColor.GRAY)
							))
			);
		}

	}
}

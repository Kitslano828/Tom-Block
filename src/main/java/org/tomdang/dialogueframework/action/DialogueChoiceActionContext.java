package org.tomdang.dialogueframework.action;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueChoice;

public record DialogueChoiceActionContext(Player player, DialogueChoice selectedChoice, DialogueContext context) {

	public DialogueChoiceActionContext {
		if (player == null) throw new IllegalArgumentException("Player should not be null");
		if (selectedChoice == null) throw new IllegalArgumentException("selectedChoice should not be null");
		if (context == null) throw new IllegalArgumentException("context should not be null");
	}

}

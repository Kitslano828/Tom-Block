package org.tomdang.dialogueframework.presentation.choice;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.session.DialogueSession;

public interface DialogueChoicePresentation {

	public void showChoices(Player player, DialogueSession session);

}

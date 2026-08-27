package org.tomdang.crafting.dialogue;

import org.bukkit.entity.Player;
import org.tomdang.crafting.gui.ForgeMenu;
import org.tomdang.dialogueframework.action.DialogueChoiceAction;
import org.tomdang.dialogueframework.action.DialogueChoiceActionContext;

public class OpenForgeDialogueAction implements DialogueChoiceAction {


	@Override
	public void execute(DialogueChoiceActionContext context) {
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		Player player = context.player();
		ForgeMenu menu = new ForgeMenu();
		menu.open(player);
	}
}

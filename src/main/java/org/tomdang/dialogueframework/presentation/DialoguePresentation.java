package org.tomdang.dialogueframework.presentation;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.session.DialogueSession;

public interface DialoguePresentation {

	public void show(Player player, DialogueSession session);

	public void close(Player player);

}

package org.tomdang.dialogueframework.presentation.hud;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.session.DialogueSession;

import java.util.UUID;

public interface DialogueTextAnimator {

	void start(Player player, DialogueSession session);

	void cancel(UUID playerUUID);

}

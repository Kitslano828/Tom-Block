package org.tomdang.dialogueframework.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;

public class DialogueSneakListener implements Listener {

	private final DialogueSessionService dialogueSessionService;
	private final DialogueAdvanceService dialogueAdvanceService;

	public DialogueSneakListener(DialogueSessionService dialogueSessionService, DialogueAdvanceService dialogueAdvanceService) {
		if (dialogueSessionService == null) throw new IllegalArgumentException("dialogueSessionService cannot be null");
		if (dialogueAdvanceService == null) throw new IllegalArgumentException("dialogueAdvanceService cannot be null");

		this.dialogueSessionService = dialogueSessionService;
		this.dialogueAdvanceService = dialogueAdvanceService;
	}

	@EventHandler
	public void playerSneakEvent(PlayerToggleSneakEvent event) {
		if (!event.isSneaking()) return;

		Player player = event.getPlayer();
		DialogueSession activeSession = dialogueSessionService.getActiveSession(player.getUniqueId());
		if (activeSession == null) return;

		dialogueAdvanceService.advance(player);
	}

}

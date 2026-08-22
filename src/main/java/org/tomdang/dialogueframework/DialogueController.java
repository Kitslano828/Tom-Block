package org.tomdang.dialogueframework;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.presentation.DialoguePresentation;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;

public class DialogueController {

	private final DialogueSessionService dialogueSessionService;
	private final DialoguePresentation dialoguePresentation;

	public DialogueController(DialogueSessionService dialogueSessionService, DialoguePresentation dialoguePresentation) {
		if (dialogueSessionService == null) throw new IllegalArgumentException("Dialogue Session Service cannot be null");
		if (dialoguePresentation == null) throw new IllegalArgumentException("Dialogue Presentation cannot be null");

		this.dialogueSessionService = dialogueSessionService;
		this.dialoguePresentation = dialoguePresentation;
	}

	public DialogueSession startDialogue(Player player, String dialogueID, DialogueContext context) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		DialogueSession dialogueSession = dialogueSessionService.startDialogue(player.getUniqueId(), dialogueID, context);
		dialoguePresentation.show(player, dialogueSession);
		return dialogueSession;
	}

	public DialogueSession selectChoice(Player player, String choiceID) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		DialogueSession session = dialogueSessionService.selectChoice(player.getUniqueId(), choiceID);
		if (session == null) {
			dialoguePresentation.close(player);
		} else {
			dialoguePresentation.show(player, session);
		}
		return session;
	}

	public DialogueSession endDialogue(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		DialogueSession session = dialogueSessionService.endDialogue(player.getUniqueId());
		dialoguePresentation.close(player);
		return session;
	}

}

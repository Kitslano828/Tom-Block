package org.tomdang.dialogueframework;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.action.DialogueChoiceActionContext;
import org.tomdang.dialogueframework.action.DialogueChoiceActionService;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.presentation.DialoguePresentation;
import org.tomdang.dialogueframework.session.DialogueChoiceSelectionResult;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.type.DialogueCompleted;

public class DialogueController {

	private final DialogueSessionService dialogueSessionService;
	private final DialoguePresentation dialoguePresentation;
	private final DialogueChoiceActionService dialogueChoiceActionService;
	private final GameplayEventBus gameplayEvents;

	public DialogueController(DialogueSessionService dialogueSessionService, DialoguePresentation dialoguePresentation, DialogueChoiceActionService dialogueChoiceActionService) {
		this(dialogueSessionService, dialoguePresentation, dialogueChoiceActionService, null);
	}

	public DialogueController(DialogueSessionService dialogueSessionService, DialoguePresentation dialoguePresentation,
	                         DialogueChoiceActionService dialogueChoiceActionService, GameplayEventBus gameplayEvents) {
		if (dialogueSessionService == null) throw new IllegalArgumentException("Dialogue Session Service cannot be null");
		if (dialoguePresentation == null) throw new IllegalArgumentException("Dialogue Presentation cannot be null");
		if (dialogueChoiceActionService == null) throw new IllegalArgumentException("dialogueChoiceActionService cannot be null");

		this.dialogueSessionService = dialogueSessionService;
		this.dialoguePresentation = dialoguePresentation;
		this.dialogueChoiceActionService = dialogueChoiceActionService;
		this.gameplayEvents = gameplayEvents;
	}

	public DialogueSession startDialogue(Player player, String dialogueID, DialogueContext context) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		DialogueSession dialogueSession = dialogueSessionService.startDialogue(player.getUniqueId(), dialogueID, context);
		dialoguePresentation.show(player, dialogueSession);
		return dialogueSession;
	}

	public DialogueChoiceSelectionResult selectChoice(Player player, String choiceID) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		DialogueChoiceSelectionResult result = dialogueSessionService.selectChoice(player.getUniqueId(), choiceID);

		if (result.getActiveSession() == null) {
			dialoguePresentation.close(player);
		} else {
			dialoguePresentation.show(player, result.getActiveSession());
		}

		DialogueChoice choice = result.getSelectedChoice();
		String actionID = choice.getActionID();
		if (actionID != null) {
			DialogueChoiceActionContext actionContext = new DialogueChoiceActionContext(player, choice, result.getDialogueContext());
			dialogueChoiceActionService.executeAction(choice.getActionID(), actionContext);
		}
		return result;
	}

	public DialogueSession endDialogue(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		DialogueSession session = dialogueSessionService.endDialogue(player.getUniqueId());
		dialoguePresentation.close(player);
		if (session != null && gameplayEvents != null) gameplayEvents.publish(new DialogueCompleted(
				player.getUniqueId(), session.getDialogueDefinition().getDialogueID(),
				session.getDialogueContext().sourceID()));
		return session;
	}

}

package org.tomdang.dialogueframework.advance;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.presentation.choice.DialogueChoicePresentation;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayState;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayStateRegistry;
import org.tomdang.dialogueframework.presentation.hud.DialogueTextAnimationService;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;

import java.util.List;
import java.util.UUID;

public class DialogueAdvanceService {

	private final DialogueSessionService dialogueSessionService;
	private final DialogueDisplayStateRegistry dialogueDisplayStateRegistry;
	private final DialogueTextAnimationService dialogueTextAnimationService;
	private final DialogueController dialogueController;
	private final DialogueChoicePresentation dialogueChoicePresentation;

	public DialogueAdvanceService(DialogueSessionService dialogueSessionService, DialogueDisplayStateRegistry dialogueDisplayStateRegistry,
	                              DialogueTextAnimationService dialogueTextAnimationService, DialogueController dialogueController, DialogueChoicePresentation dialogueChoicePresentation
	) {
		if (dialogueSessionService == null) throw new IllegalArgumentException("dialogueSessionService cannot be null");
		if (dialogueDisplayStateRegistry == null) throw new IllegalArgumentException("dialogueDisplayStateRegistry cannot be null");
		if (dialogueTextAnimationService == null) throw new IllegalArgumentException("dialogueTextAnimationService cannot be null");
		if (dialogueController == null) throw new IllegalArgumentException("dialogueController cannot be null");
		if (dialogueChoicePresentation == null) throw new IllegalArgumentException("dialogueChoicePresentation cannot be null");

		this.dialogueSessionService = dialogueSessionService;
		this.dialogueDisplayStateRegistry = dialogueDisplayStateRegistry;
		this.dialogueTextAnimationService = dialogueTextAnimationService;
		this.dialogueController = dialogueController;
		this.dialogueChoicePresentation = dialogueChoicePresentation;
	}

	public DialogueAdvanceResult advance(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");

		UUID playerUUID = player.getUniqueId();

		DialogueSession session = dialogueSessionService.getActiveSession(playerUUID);
		if (session == null) throw new IllegalStateException("No session exists");

		DialogueDisplayState displayState = dialogueDisplayStateRegistry.getState(playerUUID);
		if (displayState == null) throw new IllegalStateException("No display state exists for this player");
		if (!displayState.getDisplayNodeID().equals(session.getCurrentNodeID())) throw new IllegalStateException("Node IDs do not match");

		if (!displayState.isFullyRevealed()) {
			dialogueTextAnimationService.revealAll(player, session);
			return DialogueAdvanceResult.TEXT_REVEALED;
		}

		List<DialogueChoice> dialogueChoices = session.getCurrentNode().getDialogueChoices();
		if (dialogueChoices.isEmpty()) {
			dialogueController.endDialogue(player);
			return DialogueAdvanceResult.DIALOGUE_ENDED;
		}
		if (dialogueChoices.size() == 1) {
			DialogueChoice choice = dialogueChoices.getFirst();
			DialogueSession dialogueSession = dialogueController.selectChoice(player, choice.getChoiceID());
			if (dialogueSession == null) return DialogueAdvanceResult.DIALOGUE_ENDED;
			return DialogueAdvanceResult.NODE_ADVANCED;
		}

		if (!displayState.areChoicesShown()) {
			dialogueChoicePresentation.showChoices(player, session);
			displayState.markChoicesShown();
		}
		return DialogueAdvanceResult.CHOICE_REQUIRED;
	}

}

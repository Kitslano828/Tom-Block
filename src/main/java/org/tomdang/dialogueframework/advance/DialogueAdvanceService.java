package org.tomdang.dialogueframework.advance;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.choice.DialogueChoicePresentation;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayState;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayStateRegistry;
import org.tomdang.dialogueframework.presentation.hud.DialogueTextAnimationService;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePaginationService;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueChoiceSelectionResult;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;

import java.util.List;
import java.util.UUID;

public class DialogueAdvanceService {

	private final DialogueSessionService dialogueSessionService;
	private final DialogueDisplayStateRegistry dialogueDisplayStateRegistry;
	private final DialogueTextAnimationService dialogueTextAnimationService;
	private final DialogueController dialogueController;
	private final DialogueChoicePresentation dialogueChoicePresentation;
	private final DialoguePaginationService dialoguePaginationService;
	private final DialogueThemeRegistry dialogueThemeRegistry;
	private final DialogueHudSkinRegistry dialogueHudSkinRegistry;

	public DialogueAdvanceService(DialogueSessionService dialogueSessionService, DialogueDisplayStateRegistry dialogueDisplayStateRegistry,
	                              DialogueTextAnimationService dialogueTextAnimationService, DialogueController dialogueController, DialogueChoicePresentation dialogueChoicePresentation,
								  DialoguePaginationService dialoguePaginationService, DialogueThemeRegistry dialogueThemeRegistry, DialogueHudSkinRegistry dialogueHudSkinRegistry
	) {
		if (dialogueSessionService == null) throw new IllegalArgumentException("dialogueSessionService cannot be null");
		if (dialogueDisplayStateRegistry == null) throw new IllegalArgumentException("dialogueDisplayStateRegistry cannot be null");
		if (dialogueTextAnimationService == null) throw new IllegalArgumentException("dialogueTextAnimationService cannot be null");
		if (dialogueController == null) throw new IllegalArgumentException("dialogueController cannot be null");
		if (dialogueChoicePresentation == null) throw new IllegalArgumentException("dialogueChoicePresentation cannot be null");
		if (dialoguePaginationService == null) throw new IllegalArgumentException("dialoguePaginationService cannot be null");
		if (dialogueThemeRegistry == null) throw new IllegalArgumentException("dialogueThemeRegistry cannot be null");
		if (dialogueHudSkinRegistry == null) throw new IllegalArgumentException("dialogueHudSkinRegistry cannot be null");

		this.dialogueSessionService = dialogueSessionService;
		this.dialogueDisplayStateRegistry = dialogueDisplayStateRegistry;
		this.dialogueTextAnimationService = dialogueTextAnimationService;
		this.dialogueController = dialogueController;
		this.dialogueChoicePresentation = dialogueChoicePresentation;
		this.dialoguePaginationService = dialoguePaginationService;
		this.dialogueThemeRegistry = dialogueThemeRegistry;
		this.dialogueHudSkinRegistry = dialogueHudSkinRegistry;
	}

	public DialogueAdvanceResult advance(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");

		UUID playerUUID = player.getUniqueId();

		DialogueSession session = dialogueSessionService.getActiveSession(playerUUID);
		if (session == null) throw new IllegalStateException("No session exists");

		DialogueDisplayState displayState = dialogueDisplayStateRegistry.getState(playerUUID);
		if (displayState == null) throw new IllegalStateException("No display state exists for this player");
		if (!displayState.getDisplayNodeID().equals(session.getCurrentNodeID())) throw new IllegalStateException("Node IDs do not match");

		DialogueNode currentNode = session.getCurrentNode();
		if (currentNode == null) throw new IllegalStateException("Current dialogue node does not exist");

		String sourceID = session.getDialogueContext().sourceID();
		DialogueThemeDefinition themeDefinition = dialogueThemeRegistry.resolveThemeForSource(sourceID);
		if (themeDefinition == null) throw new IllegalStateException("No dialogue theme exists for source " + sourceID);

		String skinID = themeDefinition.hudSkinID();
		DialogueHudSkin skin = dialogueHudSkinRegistry.lookupSkin(skinID);
		if (skin == null) throw new IllegalStateException("No dialogue HUD skin exists for ID " + skinID);

		List<DialoguePage> pages = dialoguePaginationService.paginate(skin, currentNode.getDialogueText());
		int currentPageIndex = displayState.getCurrentPageIndex();
		if (currentPageIndex < 0 || currentPageIndex >= pages.size()) {
			throw new IllegalStateException("Current page index does not exist: " + currentPageIndex);
		}

		if (!displayState.isCurrentPageFullyRevealed()) {
			dialogueTextAnimationService.revealAll(player, session);
			return DialogueAdvanceResult.TEXT_REVEALED;
		}

		if (currentPageIndex + 1 < pages.size()) {
			DialoguePage nextPage = pages.get(currentPageIndex + 1);
			displayState.advancePage(pages.size(), nextPage.getBeginningIndex());
			dialogueTextAnimationService.refresh(player, session);
			return DialogueAdvanceResult.PAGE_ADVANCED;
		}

		List<DialogueChoice> dialogueChoices = currentNode.getDialogueChoices();
		if (dialogueChoices.isEmpty()) {
			dialogueController.endDialogue(player);
			return DialogueAdvanceResult.DIALOGUE_ENDED;
		}

		if (dialogueChoices.size() == 1) {
			DialogueChoice choice = dialogueChoices.getFirst();
			DialogueChoiceSelectionResult result = dialogueController.selectChoice(player, choice.getChoiceID());
			if (result.getActiveSession() == null) return DialogueAdvanceResult.DIALOGUE_ENDED;
			return DialogueAdvanceResult.NODE_ADVANCED;
		}

		if (!displayState.areChoicesShown()) {
			dialogueChoicePresentation.showChoices(player, session);
			displayState.markChoicesShown();
			dialogueTextAnimationService.refresh(player, session);
		}
		return DialogueAdvanceResult.CHOICE_REQUIRED;
	}

}

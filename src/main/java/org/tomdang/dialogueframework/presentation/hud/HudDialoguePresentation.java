package org.tomdang.dialogueframework.presentation.hud;

import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.DialoguePresentation;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePaginationService;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.player.playeractionbar.ActionBarSuppressionService;

import java.util.List;
import java.util.UUID;

public class HudDialoguePresentation implements DialoguePresentation {

	private final DialogueThemeRegistry dialogueThemeRegistry;
	private final DialogueDisplayStateRegistry dialogueDisplayStateRegistry;
	private final DialogueHudRenderer dialogueHudRenderer;
	private final DialogueHudSkinRegistry dialogueHudSkinRegistry;
	private final DialogueTextAnimator dialogueTextAnimator;
	private final ActionBarSuppressionService actionBarSuppressionService;
	private final DialoguePaginationService dialoguePaginationService;

	public HudDialoguePresentation(DialogueThemeRegistry dialogueThemeRegistry,
	                               DialogueDisplayStateRegistry dialogueDisplayStateRegistry,
	                               DialogueHudRenderer dialogueHudRenderer, DialogueHudSkinRegistry dialogueHudSkinRegistry,
	                               DialogueTextAnimator dialogueTextAnimator, ActionBarSuppressionService actionBarSuppressionService,
								   DialoguePaginationService dialoguePaginationService)
	{
		if (dialogueThemeRegistry == null) throw new IllegalArgumentException("DialogueThemeRegistry cannot be null");
		if (dialogueDisplayStateRegistry == null) throw new IllegalArgumentException("dialogueDisplayStateRegistry cannot be null");
		if (dialogueHudRenderer == null) throw new IllegalArgumentException("dialogueHudRenderer cannot be null");
		if (dialogueHudSkinRegistry == null) throw new IllegalArgumentException("dialogueHudSkinRegistry cannot be null");
		if (dialogueTextAnimator == null) throw new IllegalArgumentException("dialogueTextAnimator cannot be null");
		if (actionBarSuppressionService == null) throw new IllegalArgumentException("actionBarSuppressionService cannot be null");
		if (dialoguePaginationService == null) throw new IllegalArgumentException("dialoguePaginationService cannot be null");

		this.dialogueThemeRegistry = dialogueThemeRegistry;
		this.dialogueDisplayStateRegistry = dialogueDisplayStateRegistry;
		this.dialogueHudRenderer = dialogueHudRenderer;
		this.dialogueHudSkinRegistry = dialogueHudSkinRegistry;
		this.dialogueTextAnimator = dialogueTextAnimator;
		this.actionBarSuppressionService = actionBarSuppressionService;
		this.dialoguePaginationService = dialoguePaginationService;
	}

	@Override
	public void show(Player player, DialogueSession session) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		if (session == null) throw new IllegalArgumentException("session cannot be null");

		UUID playerUUID = player.getUniqueId();

		if (!playerUUID.equals(session.getPlayerUUID())) throw new IllegalStateException("Player UUIDs do not match");

		DialogueNode currentNode = session.getCurrentNode();
		if (currentNode == null) throw new IllegalStateException("A valid session should always have a node");

		String sourceID = session.getDialogueContext().sourceID();
		DialogueThemeDefinition themeDefinition = dialogueThemeRegistry.resolveThemeForSource(sourceID);
		if (themeDefinition == null) throw new IllegalStateException("No theme is found for " + sourceID);

		String skinID = themeDefinition.hudSkinID();
		DialogueHudSkin hudSkin = dialogueHudSkinRegistry.lookupSkin(skinID);
		if (hudSkin == null) throw new IllegalStateException("No skin is found for " + skinID);

		String completeText =currentNode.getDialogueText();

		List<DialoguePage> pages = dialoguePaginationService.paginate(hudSkin, completeText);

		actionBarSuppressionService.suppress(playerUUID);

		dialogueTextAnimator.cancel(playerUUID);
		dialogueDisplayStateRegistry.removeState(playerUUID);

		DialogueDisplayState displayState = new DialogueDisplayState(playerUUID, currentNode.getNodeID());
		dialogueDisplayStateRegistry.registerState(displayState);

		int currentPageIndex = displayState.getCurrentPageIndex();

		dialogueHudRenderer.render(player, themeDefinition, hudSkin, currentNode, pages.get(currentPageIndex),
				displayState.getRevealedCharacterCount(), DialogueHudIndicatorState.HIDDEN);
		dialogueTextAnimator.start(player, session);
	}

	@Override
	public void close(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		UUID playerUUID = player.getUniqueId();
		dialogueTextAnimator.cancel(playerUUID);
		dialogueDisplayStateRegistry.removeState(playerUUID);
		dialogueHudRenderer.clear(player);
		actionBarSuppressionService.release(playerUUID);
	}
}

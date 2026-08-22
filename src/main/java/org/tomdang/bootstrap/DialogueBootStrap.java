package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.TomBlock;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.presentation.choice.DialogueChoicePresentation;
import org.tomdang.dialogueframework.presentation.choice.chat.ChatDialogueChoicePresentation;
import org.tomdang.dialogueframework.presentation.hud.*;
import org.tomdang.dialogueframework.presentation.hud.animation.BukkitDialogueTextAnimator;
import org.tomdang.dialogueframework.presentation.hud.renderer.ActionBarDialogueHudRenderer;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.registry.DialogueRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.player.playeractionbar.ActionBarSuppressionService;

public class DialogueBootStrap {

	@Getter
	private final DialogueRegistry dialogueRegistry;
	@Getter
	private final DialogueSessionRegistry dialogueSessionRegistry;
	@Getter
	private final DialogueSessionService dialogueSessionService;
	@Getter
	private final DialogueThemeRegistry dialogueThemeRegistry;
	@Getter
	private final DialogueHudSkinRegistry dialogueHudSkinRegistry;
	@Getter
	private final DialogueDisplayStateRegistry dialogueDisplayStateRegistry;
	@Getter
	private final HudDialoguePresentation hudDialoguePresentation;
	@Getter
	private final DialogueController dialogueController;
	@Getter
	private final DialogueAdvanceService dialogueAdvanceService;

	public DialogueBootStrap(TomBlock instance, ActionBarSuppressionService actionBarSuppressionService) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (actionBarSuppressionService == null) throw new IllegalArgumentException("actionBarSuppressionService cannot be null");

		dialogueRegistry = new DialogueRegistry();
		dialogueSessionRegistry = new DialogueSessionRegistry();

		dialogueSessionService = new DialogueSessionService(dialogueRegistry, dialogueSessionRegistry);

		dialogueThemeRegistry = new DialogueThemeRegistry();
		dialogueHudSkinRegistry = new DialogueHudSkinRegistry();
		dialogueDisplayStateRegistry = new DialogueDisplayStateRegistry();

		DialogueHudRenderer dialogueHudRenderer = new ActionBarDialogueHudRenderer();

		// Temp Renderer
		DialogueTextAnimationService dialogueTextAnimationService = new DialogueTextAnimationService(
				dialogueDisplayStateRegistry,
				dialogueThemeRegistry,
				dialogueHudRenderer,
				dialogueHudSkinRegistry
		);

		DialogueTextAnimator bukkitDialogueTextAnimator = new BukkitDialogueTextAnimator(
				instance,
				dialogueTextAnimationService,
				dialogueDisplayStateRegistry,
				1,
				2,
				20
		);

		hudDialoguePresentation = new HudDialoguePresentation(
				dialogueThemeRegistry,
				dialogueDisplayStateRegistry,
				dialogueHudRenderer,
				dialogueHudSkinRegistry,
				bukkitDialogueTextAnimator,
				actionBarSuppressionService
		);

		dialogueController = new DialogueController(dialogueSessionService, hudDialoguePresentation);

		DialogueChoicePresentation dialogueChoicePresentation =
				new ChatDialogueChoicePresentation();

		dialogueAdvanceService = new DialogueAdvanceService(
				dialogueSessionService,
				dialogueDisplayStateRegistry,
				dialogueTextAnimationService,
				dialogueController,
				dialogueChoicePresentation
		);


	}

}

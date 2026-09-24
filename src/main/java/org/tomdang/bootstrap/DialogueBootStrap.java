package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.TomBlock;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.action.DialogueChoiceActionRegistry;
import org.tomdang.dialogueframework.action.DialogueChoiceActionService;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.configuration.DialogueConfigurationDefinition;
import org.tomdang.dialogueframework.configuration.DialogueConfigurationDefinitionRegistrar;
import org.tomdang.dialogueframework.configuration.DialogueConfigurationLoader;
import org.tomdang.dialogueframework.presentation.choice.DialogueChoicePresentation;
import org.tomdang.dialogueframework.presentation.choice.chat.ChatDialogueChoicePresentation;
import org.tomdang.dialogueframework.presentation.hud.*;
import org.tomdang.dialogueframework.presentation.hud.animation.BukkitDialogueTextAnimator;
import org.tomdang.dialogueframework.presentation.hud.layout.DialogueHudLayoutComposer;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePaginationService;
import org.tomdang.dialogueframework.presentation.hud.renderer.ActionBarDialogueHudRenderer;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.registry.DialogueRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.HudTextWidthService;
import org.tomdang.hud.text.HudTextWrapper;
import org.tomdang.hud.text.MinecraftDefaultTextWidthService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
	@Getter
	private final DialogueChoiceActionRegistry dialogueChoiceActionRegistry;
	@Getter
	private final DialogueChoiceActionService dialogueChoiceActionService;

	public DialogueBootStrap(TomBlock instance, PlayerActionBarService playerActionBarService) {
		if (instance == null) throw new IllegalArgumentException("Instance cannot be null");
		if (playerActionBarService == null) throw new IllegalArgumentException("playerActionBarService cannot be null");

		dialogueRegistry = new DialogueRegistry();
		dialogueSessionRegistry = new DialogueSessionRegistry();

		DialogueConfigurationLoader configurationLoader = new DialogueConfigurationLoader();
		List<DialogueConfigurationDefinition> dialogueDefinitions;
		try (InputStream configurationStream = instance.getResource("actors/dialogues.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain dialogues.yml");
			}
			dialogueDefinitions = configurationLoader.loadDefinitions(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled dialogues.yml resource", exception);
		}
		DialogueConfigurationDefinitionRegistrar definitionRegistrar =
				new DialogueConfigurationDefinitionRegistrar(dialogueRegistry);
		definitionRegistrar.registerDefinitions(dialogueDefinitions);

		dialogueSessionService = new DialogueSessionService(dialogueRegistry, dialogueSessionRegistry);

		dialogueThemeRegistry = new DialogueThemeRegistry();
		dialogueHudSkinRegistry = new DialogueHudSkinRegistry();
		dialogueDisplayStateRegistry = new DialogueDisplayStateRegistry();

		HudTextWidthService hudTextWidthService = new MinecraftDefaultTextWidthService();
		HudTextWrapper hudTextWrapper = new HudTextWrapper(hudTextWidthService);
		DialoguePaginationService dialoguePaginationService = new DialoguePaginationService(hudTextWrapper);
		DialogueVisibleLineService dialogueVisibleLineService = new DialogueVisibleLineService();
		HudSpacingService hudSpacingService = new HudSpacingService();
		DialogueHudLayoutComposer dialogueHudLayoutComposer = new DialogueHudLayoutComposer(
				hudSpacingService,
				hudTextWidthService
		);
		DialogueHudRenderer dialogueHudRenderer = new ActionBarDialogueHudRenderer(
				dialogueVisibleLineService,
				dialogueHudLayoutComposer,
				playerActionBarService
		);

		DialogueTextAnimationService dialogueTextAnimationService = new DialogueTextAnimationService(
				dialogueDisplayStateRegistry,
				dialogueThemeRegistry,
				dialogueHudRenderer,
				dialogueHudSkinRegistry,
				dialoguePaginationService
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
				dialoguePaginationService
		);

		dialogueChoiceActionRegistry = new DialogueChoiceActionRegistry();
		dialogueChoiceActionService = new DialogueChoiceActionService(dialogueChoiceActionRegistry);

		dialogueController = new DialogueController(dialogueSessionService, hudDialoguePresentation, dialogueChoiceActionService);



		DialogueChoicePresentation dialogueChoicePresentation =
				new ChatDialogueChoicePresentation();

		dialogueAdvanceService = new DialogueAdvanceService(
				dialogueSessionService,
				dialogueDisplayStateRegistry,
				dialogueTextAnimationService,
				dialogueController,
				dialogueChoicePresentation,
				dialoguePaginationService,
				dialogueThemeRegistry,
				dialogueHudSkinRegistry
		);


	}

}

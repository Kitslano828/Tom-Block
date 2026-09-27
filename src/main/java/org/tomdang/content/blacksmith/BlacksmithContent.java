package org.tomdang.content.blacksmith;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.interaction.LookAtPlayerInteraction;
import org.tomdang.actorframework.movement.ActorLookService;
import org.tomdang.crafting.dialogue.OpenForgeDialogueAction;
import org.tomdang.crafting.interaction.OpenForgeInteraction;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.action.DialogueChoiceActionRegistry;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.integration.actor.ActorDialogueInteraction;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorStyle;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.hud.glyph.HudGlyph;
import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.lifecycle.ModuleContext;
import org.tomdang.platform.lifecycle.TomBlockModule;

import java.util.List;
import java.util.Map;

public class BlacksmithContent implements TomBlockModule {
	public static final ContentKey<TomBlockModule> MODULE_ID = ContentKey.of("tomblock", "blacksmith-content");
	private final DialogueThemeRegistry dialogueThemeRegistry;
	private final DialogueHudSkinRegistry dialogueHudSkinRegistry;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessionService;
	private final DialogueAdvanceService dialogueAdvanceService;
	private final ActorInteractionRegistry actorInteractionRegistry;
	private final ActorLookService actorLookService;
	private final DialogueChoiceActionRegistry dialogueChoiceActionRegistry;

	public BlacksmithContent(DialogueThemeRegistry dialogueThemeRegistry, DialogueHudSkinRegistry dialogueHudSkinRegistry,
	                         DialogueController dialogueController, DialogueSessionService dialogueSessionService, DialogueAdvanceService dialogueAdvanceService,
	                         ActorInteractionRegistry actorInteractionRegistry,
							 ActorLookService actorLookService, DialogueChoiceActionRegistry dialogueChoiceActionRegistry
	) {
		if (dialogueThemeRegistry == null) throw new IllegalArgumentException("dialogueThemeRegistry cannot be null");
		if (dialogueHudSkinRegistry == null) throw new IllegalArgumentException("dialogueHudSkinRegistry cannot be null");
		if (dialogueController == null) throw new IllegalArgumentException("dialogueController cannot be null");
		if (dialogueSessionService == null) throw new IllegalArgumentException("dialogueSessionService cannot be null");
		if (dialogueAdvanceService == null) throw new IllegalArgumentException("dialogueAdvanceService cannot be null");
		if (actorInteractionRegistry == null) throw new IllegalArgumentException("actorInteractionRegistry cannot be null");
		if (actorLookService == null) throw new IllegalArgumentException("actorLookService cannot be null");
		if (dialogueChoiceActionRegistry == null) throw new IllegalArgumentException("dialogueChoiceActionRegistry cannot be null");

		this.dialogueThemeRegistry = dialogueThemeRegistry;
		this.dialogueHudSkinRegistry = dialogueHudSkinRegistry;
		this.dialogueController = dialogueController;
		this.dialogueSessionService = dialogueSessionService;
		this.dialogueAdvanceService = dialogueAdvanceService;
		this.actorInteractionRegistry = actorInteractionRegistry;
		this.actorLookService = actorLookService;
		this.dialogueChoiceActionRegistry = dialogueChoiceActionRegistry;
	}

	public void register() {
		OpenForgeInteraction openForgeInteraction = new OpenForgeInteraction();
		actorInteractionRegistry.registerInteraction("BLACKSMITH_INTERACTION", openForgeInteraction);

		OpenForgeDialogueAction openForgeDialogueAction = new OpenForgeDialogueAction();
		dialogueChoiceActionRegistry.registerAction("OPEN_FORGE", openForgeDialogueAction);

		HudGlyph hudGlyph = new HudGlyph(Key.key("tomblock", "dialogue"), "\uE001", 256);
		List<Key> dialogueLineFonts = List.of(
				Key.key("tomblock", "dialogue_line_1"),
				Key.key("tomblock", "dialogue_line_2"),
				Key.key("tomblock", "dialogue_line_3")
		);

		Key blacksmithFont = Key.key("tomblock", "dialogue_speaker");
		Key dialogueIndicatorFont = Key.key("tomblock", "dialogue_indicator");

		DialogueHudIndicatorStyle continueIndicatorStyle = new DialogueHudIndicatorStyle(
				"»", dialogueIndicatorFont, TextColor.fromHexString("#FFF1D0"), 12
		);
		DialogueHudIndicatorStyle choiceIndicatorStyle = new DialogueHudIndicatorStyle(
				"?", dialogueIndicatorFont, TextColor.fromHexString("#FFF1D0"), 12
		);
		DialogueHudSkin hudSkin = new DialogueHudSkin("BLACKSMITH_BOX", hudGlyph, 12, 12, dialogueLineFonts,
				blacksmithFont, 12, 12, TextColor.fromHexString("#FFFFFF"), TextColor.fromHexString("#3F3F3F"),
				Map.of(
						DialogueHudIndicatorState.CONTINUE, continueIndicatorStyle,
						DialogueHudIndicatorState.CHOICE_REQUIRED, choiceIndicatorStyle
				));
		dialogueHudSkinRegistry.registerSkin(hudSkin);
		DialogueThemeDefinition themeDefinition = new DialogueThemeDefinition("BLACKSMITH_THEME", "Blacksmith", "BLACKSMITH_BOX");
		dialogueThemeRegistry.registerTheme(themeDefinition);
		dialogueThemeRegistry.bindSource("BLACKSMITH", "BLACKSMITH_THEME");

		ActorDialogueInteraction interaction = new ActorDialogueInteraction("BLACKSMITH_TEST_DIALOGUE", dialogueController, dialogueSessionService, dialogueAdvanceService);

		LookAtPlayerInteraction lookInteraction = new LookAtPlayerInteraction(actorLookService, interaction);
		actorInteractionRegistry.registerInteraction("BLACKSMITH_DIALOGUE_INTERACTION", lookInteraction);

		dialogueThemeRegistry.bindSource("TALKING_BLACKSMITH", "BLACKSMITH_THEME");

	}

	@Override public ContentKey<TomBlockModule> id() { return MODULE_ID; }
	@Override public void register(ModuleContext context) { register(); }

}

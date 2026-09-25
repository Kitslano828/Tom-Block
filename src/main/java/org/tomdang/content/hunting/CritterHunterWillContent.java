package org.tomdang.content.hunting;

import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.interaction.LookAtPlayerInteraction;
import org.tomdang.actorframework.movement.ActorLookService;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.dialogueframework.integration.actor.ActorDialogueInteraction;
import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.lifecycle.ModuleContext;
import org.tomdang.platform.lifecycle.TomBlockModule;
import org.tomdang.quest.integration.QuestOfferDialogueInteraction;
import org.tomdang.quest.progress.QuestProgressService;

/** Owns Will's existing presentation and interaction wiring until hunting becomes its own module. */
public final class CritterHunterWillContent implements TomBlockModule {
	public static final ContentKey<TomBlockModule> MODULE_ID = ContentKey.of("tomblock", "critter-hunter-will-content");
	private final DialogueThemeRegistry themes;
	private final ActorInteractionRegistry interactions;
	private final ActorLookService actorLookService;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessions;
	private final DialogueAdvanceService dialogueAdvance;
	private final QuestProgressService quests;

	public CritterHunterWillContent(DialogueThemeRegistry themes, ActorInteractionRegistry interactions,
			ActorLookService actorLookService, DialogueController dialogueController,
			DialogueSessionService dialogueSessions, DialogueAdvanceService dialogueAdvance,
			QuestProgressService quests) {
		if (themes == null || interactions == null || actorLookService == null
				|| dialogueController == null || dialogueSessions == null || dialogueAdvance == null || quests == null) {
			throw new IllegalArgumentException("Critter Hunter Will dependencies cannot be null");
		}
		this.themes = themes;
		this.interactions = interactions;
		this.actorLookService = actorLookService;
		this.dialogueController = dialogueController;
		this.dialogueSessions = dialogueSessions;
		this.dialogueAdvance = dialogueAdvance;
		this.quests = quests;
	}

	@Override public ContentKey<TomBlockModule> id() { return MODULE_ID; }

	@Override
	public void register(ModuleContext context) {
		DialogueThemeDefinition theme = new DialogueThemeDefinition(
				"CRITTER_HUNTER_WILL_THEME", "Will", "BLACKSMITH_BOX");
		themes.registerTheme(theme);
		themes.bindSource("CRITTER_HUNTER_WILL", theme.themeID());
		interactions.registerInteraction("CRITTER_HUNTER_WILL_QUEST",
				new LookAtPlayerInteraction(actorLookService,
						new QuestOfferDialogueInteraction("INTRO_TO_HUNTING", "RETURN",
								interaction("CRITTER_HUNTER_WILL_INTRO"),
								interaction("CRITTER_HUNTER_WILL_REMINDER"),
								interaction("CRITTER_HUNTER_WILL_RETURN"),
								interaction("CRITTER_HUNTER_WILL_COMPLETE"), quests)));
	}

	private ActorDialogueInteraction interaction(String dialogueId) {
		return new ActorDialogueInteraction(dialogueId, dialogueController, dialogueSessions, dialogueAdvance);
	}
}

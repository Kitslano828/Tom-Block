package org.tomdang.quest.integration;

import org.tomdang.actorframework.interaction.ActorInteraction;
import org.tomdang.actorframework.interaction.ActorInteractionContext;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.progress.QuestSignal;

public final class QuestActorInteraction implements ActorInteraction {
	private final QuestProgressService quests;
	private final String actorId;
	private final String dialogueId;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessions;
	private final DialogueAdvanceService dialogueAdvance;

	public QuestActorInteraction(QuestProgressService quests, String actorId, String dialogueId,
	                             DialogueController dialogueController,
	                             DialogueSessionService dialogueSessions,
	                             DialogueAdvanceService dialogueAdvance) {
		this.quests = quests;
		this.actorId = actorId;
		this.dialogueId = dialogueId;
		this.dialogueController = dialogueController;
		this.dialogueSessions = dialogueSessions;
		this.dialogueAdvance = dialogueAdvance;
	}

	@Override public void interact(ActorInteractionContext context) {
		quests.signal(context.player().getUniqueId(), QuestSignal.one(QuestObjectiveType.INTERACT_WITH_ACTOR, actorId));
		quests.signal(context.player().getUniqueId(), QuestSignal.one(QuestObjectiveType.RETURN_TO_ACTOR, actorId));
		DialogueSession active = dialogueSessions.getActiveSession(context.player().getUniqueId());
		if (active == null) {
			dialogueController.startDialogue(context.player(), dialogueId,
					DialogueContext.actor(actorId, context.instance().getInstanceID()));
			return;
		}
		if (actorId.equals(active.getDialogueContext().sourceID())) {
			dialogueAdvance.advance(context.player());
		}
	}
}

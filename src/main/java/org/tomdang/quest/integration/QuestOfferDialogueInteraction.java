package org.tomdang.quest.integration;

import org.tomdang.actorframework.interaction.ActorInteraction;
import org.tomdang.actorframework.interaction.ActorInteractionContext;
import org.tomdang.dialogueframework.integration.actor.ActorDialogueInteraction;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.progress.QuestStatus;

/** Selects an NPC dialogue from the player's current state in a single offered quest. */
public final class QuestOfferDialogueInteraction implements ActorInteraction {
	private final String questId;
	private final String returnStageId;
	private final ActorDialogueInteraction offer;
	private final ActorDialogueInteraction reminder;
	private final ActorDialogueInteraction handIn;
	private final ActorDialogueInteraction completed;
	private final QuestProgressService quests;

	public QuestOfferDialogueInteraction(String questId, String returnStageId,
			ActorDialogueInteraction offer, ActorDialogueInteraction reminder,
			ActorDialogueInteraction handIn, ActorDialogueInteraction completed,
			QuestProgressService quests) {
		this.questId = require(questId, "questId");
		this.returnStageId = require(returnStageId, "returnStageId");
		this.offer = java.util.Objects.requireNonNull(offer);
		this.reminder = java.util.Objects.requireNonNull(reminder);
		this.handIn = java.util.Objects.requireNonNull(handIn);
		this.completed = java.util.Objects.requireNonNull(completed);
		this.quests = java.util.Objects.requireNonNull(quests);
	}

	@Override public void interact(ActorInteractionContext context) {
		var state = quests.progress(context.player().getUniqueId(), questId);
		if (state.isEmpty()) { offer.interact(context); return; }
		var progress = state.get();
		if (progress.status() == QuestStatus.COMPLETED) { completed.interact(context); return; }
		if (returnStageId.equals(progress.currentStageId())) { handIn.interact(context); return; }
		reminder.interact(context);
	}

	private static String require(String value, String name) {
		if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " cannot be blank");
		return value;
	}
}

package org.tomdang.dialogueframework.integration.actor;

import org.bukkit.entity.Player;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.interaction.ActorInteraction;
import org.tomdang.actorframework.interaction.ActorInteractionContext;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;

public class ActorDialogueInteraction implements ActorInteraction {

	private final String dialogueID;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessionService;
	private final DialogueAdvanceService dialogueAdvanceService;

	public ActorDialogueInteraction(String dialogueID, DialogueController dialogueController,
									DialogueSessionService dialogueSessionService, DialogueAdvanceService dialogueAdvanceService
	) {

		if (dialogueID == null) throw new IllegalArgumentException("dialogueID cannot be null");
		if (dialogueID.isBlank()) throw new IllegalArgumentException("dialogueID cannot be blank");
		if (dialogueController == null) throw new IllegalArgumentException("dialogueController cannot be null");
		if (dialogueSessionService == null) throw new IllegalArgumentException("DialogueSessionService cannot be null");
		if (dialogueAdvanceService == null) throw new IllegalArgumentException("dialogueAdvanceService cannot be null");

		this.dialogueID = dialogueID;
		this.dialogueController = dialogueController;
		this.dialogueSessionService = dialogueSessionService;
		this.dialogueAdvanceService = dialogueAdvanceService;
	}

	@Override
	public void interact(ActorInteractionContext context) {
		if (context == null) throw new IllegalArgumentException("context cannot be null");

		Player player = context.player();
		ActorInstance instance = context.instance();

		DialogueSession session = dialogueSessionService.getActiveSession(player.getUniqueId());
		if (session == null) {
			DialogueContext dialogueContext = DialogueContext.actor(instance.getActorDefinition().getActorID(), instance.getInstanceID());
			dialogueController.startDialogue(player, dialogueID, dialogueContext);
			return;
		}

		if (instance.getInstanceID().equals(session.getDialogueContext().sourceInstanceID())) {
			dialogueAdvanceService.advance(player);
		} else {
			throw new IllegalStateException("Source InstanceID does not match with instanceID");
		}
	}
}
